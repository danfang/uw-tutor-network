import json
from models import Model
from sqlalchemy import select

if __name__ == '__main__':
	model = Model()
	model.connect()

	conn = model.engine.connect()

	conn.execute(model.schools.insert(), {
		'name': 'uw',
		'full_name': 'University of Washington',
		'type': 'University'
	})

	data = json.loads(open('../scripts/output/courses.json').read())
	num_colleges, c_count = len(data['colleges']), 0

	for college in data['colleges']:

		conn.execute(model.colleges.insert(), {
			'id': college['id'],
			'name': college['name'],
			'school': data['name']
		})

		num_majors, m_count = len(college['majors']), 0

		for major in college['majors']:

			exists = conn.execute(
				select([model.majors.c.id]).where(
					model.majors.c.link == major['link']
				)
			).fetchone()

			if not exists:

				conn.execute(model.majors.insert(), {
					'id': major['abbrev'],
					'college': college['id'],
					'name': major['name'],
					'link':	major['link']
				})

				for course in major['courses']:

					course_data = {
						'id': course['id'],
						'name': course['name'],
						'plan_link': course['plan_link'],
						'major': major['abbrev']
					}

					for field in ['description', 'offered', 'prereqs']:
						if field in course and course[field]:
							course_data[field] = course[field]

					conn.execute(model.courses.insert(), course_data)

			m_count += 1
			print '\tFinished {} ({}/{}): '.format(major['name'], m_count, num_majors)

		c_count += 1
		print 'Finished {}: ({}/{})'.format(college['name'], c_count, num_colleges)

