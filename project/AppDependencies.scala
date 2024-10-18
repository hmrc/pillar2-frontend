import sbt.*

object AppDependencies {
  val mongoVersion     = "1.7.0"
  val bootstrapVersion = "8.4.0"
  val compile: Seq[ModuleID] = Seq(
    play.sbt.PlayImport.ws,
    "uk.gov.hmrc"           %% "play-frontend-hmrc-play-30"            % "8.5.0",
    "uk.gov.hmrc"           %% "play-conditional-form-mapping-play-30" % "2.0.0",
    "uk.gov.hmrc"           %% "bootstrap-frontend-play-30"            % bootstrapVersion,
    "uk.gov.hmrc.mongo"     %% "hmrc-mongo-play-30"                    % mongoVersion,
    "com.typesafe.play"     %% "play-json-joda"                        % "2.10.4",
    "org.julienrf"          %% "play-json-derived-codecs"              % "10.0.2",
    "org.typelevel"         %% "cats-core"                             % "2.10.0",
    "org.apache.xmlgraphics" % "fop"                                   % "2.7"
  )

  val test: Seq[ModuleID] = Seq(
    "org.scalatestplus"    %% "mockito-4-11"             % "3.2.18.0",
    "org.jsoup"             % "jsoup"                    % "1.17.2",
    "uk.gov.hmrc"          %% "bootstrap-test-play-30"   % bootstrapVersion,
    "org.mockito"          %% "mockito-scala"            % "1.17.31",
    "uk.gov.hmrc.mongo"    %% "hmrc-mongo-test-play-30"  % mongoVersion,
    "org.scalatestplus"    %% "scalatestplus-scalacheck" % "3.1.0.0-RC2",
    "com.danielasfregola"  %% "random-data-generator"    % "2.9",
    "io.github.wolfendale" %% "scalacheck-gen-regexp"    % "1.1.0"
  ).map(_ % Test)

  val it: Seq[ModuleID] = Seq(
    "uk.gov.hmrc" %% "bootstrap-test-play-30" % bootstrapVersion % Test
  )

  def apply(): Seq[ModuleID] = compile ++ test
}
