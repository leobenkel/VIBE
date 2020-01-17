
deep_clean:
	rm -fr ./target ; rm -fr ./**/target

start:
	sbt "; client/debugDist ; ~server/reStart"

build_front:
	sbt ~client/debugDist

build_front_prod:
	sbt client/dist

fmt:
	sbt scalafmt
