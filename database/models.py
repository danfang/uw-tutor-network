import os
from sqlalchemy import *
from sqlalchemy.types import Text

class Model(object):
	
	def __init__(self):
		metadata = MetaData()

		self.schools = Table('schools', metadata,
			Column('name', String, primary_key=True),
			Column('full_name', String, nullable=False, unique=True),
			Column('type', String, nullable=False)
		)

		self.colleges = Table('colleges', metadata,
			Column('id', String),
			Column('name', String, nullable=False),
			Column('school', String, ForeignKey('schools.name')),
			PrimaryKeyConstraint('id', 'school')
		)

		self.majors = Table('majors', metadata,
			Column('id', String),
			Column('college', String),
			Column('school', String),
			Column('name', String, nullable=False),
			Column('link', String),
			ForeignKeyConstraint(
				['college', 'school'],
			 	['colleges.id', 'colleges.school']
			),
			PrimaryKeyConstraint('id', 'college', 'school')
		)

		self.courses = Table('courses', metadata,
			Column('id', String),
			Column('major', String),
			Column('college', String),
			Column('school', String),
			Column('name', String, nullable=False),
			Column('description', Text),
			Column('offered', String),
			Column('plan_link', String),
			Column('prereqs', String),
			ForeignKeyConstraint(
				['major', 'college', 'school'],
				['majors.id', 'majors.college', 'majors.school']
			),
			PrimaryKeyConstraint('id', 'major', 'college', 'school')
		)

		print 'Created course models.'

		self.users = Table('users', metadata,
			Column('first_name', String),
			Column('last_name', String),
			Column('email', String, primary_key=True),
			Column('password', String, nullable=False),
			Column('verified', Boolean, nullable=False, default=False),
			Column('student', Boolean, nullable=False, default=True),
			Column('tutor', Boolean, nullable=False, default=False),
			Column('rate', Integer),
			Column('about', String(500))
		)

		self.tutors = Table('tutors', metadata,
			Column('user', String, ForeignKey('users.email')),
			Column('course', String),
			Column('major', String),
			Column('college', String),
			Column('school', String),
			Column('rate', Integer),
			PrimaryKeyConstraint('user', 'course', 'major', 'college', 'school'),
			ForeignKeyConstraint(
				['course', 'major', 'college', 'school'],
				['courses.id', 'courses.major', 'courses.college', 'courses.school']
			)
		)

		print 'Created tutor models.'

		self.metadata = metadata
		self.connected = False

	def connect(self):

		self.engine = create_engine(
			'postgresql+psycopg2://postgres:' + \
			os.environ['DB_PASSWORD'] + '@localhost:5432/uwtn'
		)

		print 'Connected to database.'
		self.connected = True

	def build(self):
		if not self.connected:
			self.connect()

		self.metadata.drop_all(self.engine)
		print 'Dropped old tables.'

		self.metadata.create_all(self.engine)
		print 'Updated schema.'


if __name__ == '__main__':
	model = Model()
	model.connect()
	model.build()