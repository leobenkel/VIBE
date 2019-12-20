


start:
	sbt ~server/reStart

build_front:
	sbt ~client/debugDist

build_front_prod:
	sbt client/dist
