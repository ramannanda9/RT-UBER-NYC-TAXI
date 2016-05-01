name         := "LYFT-ANALYTICS"
version      := "1.0"
organization := "edu.nyu.realtimebd.analytics"

scalaVersion := "2.10.4"

libraryDependencies ++= Seq("org.apache.spark" %% "spark-core" % "1.5.0", 
"org.apache.spark" %% "spark-sql" % "1.5.0", 
 "org.apache.spark" %% "spark-hive" % "1.5.0", 
 "org.apache.spark" %% "spark-mllib" % "1.5.0" )
resolvers += Resolver.mavenLocal