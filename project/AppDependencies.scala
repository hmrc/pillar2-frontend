import sbt.*

object AppDependencies {
  val mongoVersion     = "2.6.0"
  val bootstrapVersion = "9.13.0"
  val compile: Seq[ModuleID] = Seq(
    play.sbt.PlayImport.ws,
    "uk.gov.hmrc"           %% "play-frontend-hmrc-play-30"            % "12.5.0",
    "uk.gov.hmrc"           %% "play-conditional-form-mapping-play-30" % "3.3.0",
    "uk.gov.hmrc"           %% "bootstrap-frontend-play-30"            % bootstrapVersion,
    "uk.gov.hmrc.mongo"     %% "hmrc-mongo-play-30"                    % mongoVersion,
    "org.typelevel"         %% "cats-core"                             % "2.13.0",
    "org.apache.xmlgraphics" % "fop"                                   % "2.10",
    "commons-io"             % "commons-io"                            % "2.18.0",
    "com.beachape"          %% "enumeratum-play-json"                  % "1.9.0"
  )

  val test: Seq[ModuleID] = Seq(
    "org.scalatestplus"    %% "mockito-4-11"             % "3.2.18.0",
    "org.jsoup"             % "jsoup"                    % "1.18.3",
    "uk.gov.hmrc"          %% "bootstrap-test-play-30"   % bootstrapVersion,
    "org.mockito"          %% "mockito-scala"            % "1.17.37",
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
