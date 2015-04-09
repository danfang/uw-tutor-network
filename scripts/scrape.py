"""
Scraper for the UW Course Catalog into JSON

Example JSON output:
	{"colleges": [... {
		"id": "PubHlth",
		"name": "School of Public Health",
		"majors": [... {
			"name": "Biostatistics",
			"abbrev": "BIOST",
			"link": "biostat.html",
			"courses": [... {
				"description": "Seminar series covering technical...",
				"offered": "jointly with STAT 598; ASp", 
                "prereqs": "entry code", 
                "plan_link": "https://uwstudent.washington.edu/student/myplan/course/BIOST598", 
                "id": "biost598", 
                "name": "BIOST 598 Techniques of Statistical Consulting (1)"
			}... ]
		}... ]

	}... ]}

@author Daniel Fang <danfang@uw.edu>
"""

import requests
import re
import json
from bs4 import BeautifulSoup

CAT_LINK = 'http://www.washington.edu/students/crscat/'

def main():
	crscat_soup = BeautifulSoup(requests.get(CAT_LINK).text)

	colleges = crscat_soup.find_all('a', 
		attrs={'name': True},
		text=re.compile(r'.*(School|College).*')
	)

	total = {}
	total['name'] = 'uw'
	total['colleges'] = get_colleges(colleges)
	output = open('output/courses.json', 'w')
	output.write(json.dumps(total, indent=4))
	output.close()

def get_colleges(colleges):
	college_arr = []

	for college in colleges:
		college_id = college.attrs['name']

		college_obj = {
			'id': college_id,
			'name': college.text
		}

		majors = college.find_next('ul')
		college_obj['majors'] = get_majors(majors)
		college_arr.append(college_obj)

	return college_arr


def get_majors(majors):
	major_arr = []

	for major in majors.find_all(href=re.compile(r'.*\.htm')):
		major_link = major.attrs['href']
		major_obj = {
			'link': major_link,
		}
		major_name = re.sub(r'[\xa0]', ' ', major.text)
		abbrev_match = re.search(r'\(([^a-z]*)\)', major_name)

		# only taking courses with valid abbreviation
		if abbrev_match:
			major_name = re.sub(re.escape(abbrev_match.group(0)), '', major_name)
			major_obj['abbrev'] = abbrev_match.group(1)

			major_obj['name'] = major_name.strip()

			print major_obj

			course_soup = BeautifulSoup(requests.get(CAT_LINK + major_link).text)
			courses = course_soup.find_all('a', attrs={'name': True})
			major_obj['courses'] = get_courses(courses)
			major_arr.append(major_obj)

	return major_arr

def get_courses(courses):
	courses_arr = []

	for course in courses:
		course_obj = {
			'id': course.attrs['name'],
			'name': course.p.b.text,
			'plan_link': course.p.a.attrs['href']
		}

		course_description = course.p.b.find_next_sibling(text=True)

		if course_description:

			offered_match = re.search(r'Offered: ([^.]*)\.', course_description)

			if offered_match:
				course_description = re.sub(re.escape(offered_match.group(0)), '', course_description).strip()
				course_obj['offered'] = offered_match.group(1)

			prereq_match = re.search(r'Prerequisite: (.*)\.$', course_description)

			if prereq_match: 
				course_description = re.sub(re.escape(prereq_match.group(0)), '', course_description)
				course_obj['prereqs'] = prereq_match.group(1).strip()

			course_obj['description'] = course_description.strip()

		courses_arr.append(course_obj)

	return courses_arr

if __name__ == '__main__':
	main()