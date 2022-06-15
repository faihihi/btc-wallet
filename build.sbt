lazy val akkaHttpVersion = "10.2.9"
lazy val akkaVersion     = "2.6.14"
lazy val akkaStreamKafka = "2.1.0"
lazy val scaffeine       = "4.0.2"

fork := true
scalafmtOnCompile := true

lazy val root = (project in file(".")).settings(
  inThisBuild(
    List(
      organization := "com.btc",
      scalaVersion := "2.13.4"
    )
  ),
  name := "btc-wallet",
  scalacOptions ++= Seq("-target:jvm-1.8"),
  libraryDependencies ++= Seq(
    "com.typesafe.akka"          %% "akka-http"                    % akkaHttpVersion,
    "com.typesafe.akka"          %% "akka-http-spray-json"         % akkaHttpVersion,
    "com.typesafe.akka"          %% "akka-actor-typed"             % akkaVersion,
    "com.typesafe.akka"          %% "akka-stream"                  % akkaVersion,
    "com.typesafe.akka"          %% "akka-persistence"             % akkaVersion,
    "com.typesafe.akka"          %% "akka-persistence-query"       % akkaVersion,
    "com.typesafe.akka"          %% "akka-cluster-tools"           % akkaVersion,
    "com.typesafe.akka"          %% "akka-persistence-cassandra"   % "0.103",
    "com.datastax.oss"            % "java-driver-core"             % "4.14.1",
    "com.typesafe.akka"          %% "akka-stream-kafka"            % akkaStreamKafka,
    "com.github.blemale"         %% "scaffeine"                    % scaffeine,
    "ch.qos.logback"              % "logback-classic"              % "1.2.3",
    "com.typesafe.scala-logging" %% "scala-logging"                % "3.9.5",
    "com.codahale.metrics"        % "metrics-core"                 % "3.0.2",
    "io.netty"                    % "netty-transport-native-epoll" % "4.1.77.Final",
    "org.scaldi"                 %% "scaldi"                       % "0.6.0",
    "org.typelevel"              %% "cats-core"                    % "2.7.0",
    "org.typelevel"              %% "cats-effect"                  % "3.3.12",
    "joda-time"                   % "joda-time"                    % "2.10.14",
//    "org.scala-lang.modules"     %% "scala-java8-compat"           % "1.0.2",
    "com.typesafe.akka"          %% "akka-http-testkit"            % akkaHttpVersion % Test,
    "com.typesafe.akka"          %% "akka-actor-testkit-typed"     % akkaVersion     % Test,
    "org.scalatest"              %% "scalatest"                    % "3.1.4"         % Test,
    "org.mockito"                %% "mockito-scala-scalatest"      % "1.17.7"        % Test
  )
)
