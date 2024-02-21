import sbt._

object AppDependencies {
  import play.core.PlayVersion
  val mongoVersion = "1.5.0"
  val bootstrapVersion="7.23.0"
  val compile = Seq(
    play.sbt.PlayImport.ws,
    "uk.gov.hmrc"       %% "play-frontend-hmrc-play-28"     % "8.5.0",
    "uk.gov.hmrc"       %% "play-conditional-form-mapping"  % "1.13.0-play-28",
    "uk.gov.hmrc"       %% "bootstrap-frontend-play-28"     % bootstrapVersion,
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-28"             % mongoVersion,
    "com.typesafe.play" %% "play-json-joda"                 % "2.9.4",
    "org.julienrf"      %% "play-json-derived-codecs"       % "10.0.2"
  )

  val test = Seq(

    "org.scalatestplus"       %% "mockito-3-4"              % "3.2.10.0",
    "io.github.wolfendale"    %% "scalacheck-gen-regexp"    % "0.1.3",
    "org.scalatestplus.play"  %% "scalatestplus-play"       % "5.1.0",
    "org.jsoup"               %  "jsoup"                    % "1.14.3",
    "com.typesafe.play"       %% "play-test"                % PlayVersion.current,
    "uk.gov.hmrc"             %% "bootstrap-test-play-28"   % bootstrapVersion,
    "org.mockito"             %% "mockito-scala"            % "1.16.42",
    "uk.gov.hmrc.mongo"       %% "hmrc-mongo-test-play-28"  % mongoVersion,
    "com.vladsch.flexmark"    %  "flexmark-all"             % "0.62.2",
    "org.scalatestplus"       %% "scalatestplus-scalacheck" % "3.1.0.0-RC2",
    "com.danielasfregola"     %% "random-data-generator"    % "2.9",
    "io.github.wolfendale"    %% "scalacheck-gen-regexp"    % "1.1.0"

  ).map(_ % "test, it")

  def apply(): Seq[ModuleID] = compile ++ test
}
