import os
from sqlalchemy import *
from sqlalchemy.types import Text

class Model(object):

	def __init__(self):
		metadata = MetaData()

		self.schools = Table('schools', metadata,
			Column('name', String, primary_key=True),
			Column('full_name', String, nullable=False),
			Column('type', String, nullable=False)
		)

		self.colleges = Table('colleges', metadata,
			Column('id', String, primary_key=True),
			Column('name', String, nullable=False),
			Column('school', String, ForeignKey('schools.name'), nullable=False)
		)

		self.majors = Table('majors', metadata,
			Column('id', String, primary_key=True),
			Column('college', String, ForeignKey('colleges.id'), nullable=False),
			Column('name', String, nullable=False),
			Column('link', String)
		)

		self.courses = Table('courses', metadata,
			Column('id', String, primary_key=True),
			Column('major', String, ForeignKey('majors.id'), nullable=False),
			Column('name', String, nullable=False),
			Column('description', Text),
			Column('offered', String),
			Column('plan_link', String),
			Column('prereqs', String)
		)

		print 'Created models.'

		self.metadata = metadata
		self.connected = False

	def connect(self):

		self.engine = create_engine(
			'postgresql+psycopg2://postgres:' + \
			os.environ['DB_PASSWORD'] + '@localhost:5432/uwtn'
		)

		self.connected = True

		print 'Connected to database.'

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