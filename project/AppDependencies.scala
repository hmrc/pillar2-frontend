import sbt.*

object AppDependencies {
  val bootstrapVersion                      = "9.11.0"
  val catsCoreVersion                       = "2.13.0"
  val commonsIoVersion                      = "2.19.0"
  val enumeratumPlayJsonVersion             = "1.8.2"
  val fopVersion                            = "2.10"
  val jsoupVersion                          = "1.20.1"
  val mockitoVersion                        = "3.2.18.0"
  val mockitoScalaVersion                   = "1.17.37"
  val mongoVersion                          = "2.6.0"
  val playConditionalFormMappingPlayVersion = "3.3.0"
  val playFrontendHmrcPlayVersion           = "12.1.0"
  val randomDataGeneratorVersion            = "2.9"
  val scalacheckGenRegexpVersion            = "1.1.0"
  val scalatestplusScalacheckVersion        = "3.1.0.0-RC2"

  val compile: Seq[ModuleID] = Seq(
    play.sbt.PlayImport.ws,
    "uk.gov.hmrc"           %% "play-frontend-hmrc-play-30"            % playFrontendHmrcPlayVersion,
    "uk.gov.hmrc"           %% "play-conditional-form-mapping-play-30" % playConditionalFormMappingPlayVersion,
    "uk.gov.hmrc"           %% "bootstrap-frontend-play-30"            % bootstrapVersion,
    "uk.gov.hmrc.mongo"     %% "hmrc-mongo-play-30"                    % mongoVersion,
    "org.typelevel"         %% "cats-core"                             % catsCoreVersion,
    "org.apache.xmlgraphics" % "fop"                                   % fopVersion,
    "commons-io"             % "commons-io"                            % commonsIoVersion,
    "com.beachape"          %% "enumeratum-play-json"                  % enumeratumPlayJsonVersion
  )

  val test: Seq[ModuleID] = Seq(
    "org.scalatestplus"    %% "mockito-4-11"             % mockitoVersion,
    "org.jsoup"             % "jsoup"                    % jsoupVersion,
    "uk.gov.hmrc"          %% "bootstrap-test-play-30"   % bootstrapVersion,
    "org.mockito"          %% "mockito-scala"            % mockitoScalaVersion,
    "uk.gov.hmrc.mongo"    %% "hmrc-mongo-test-play-30"  % mongoVersion,
    "org.scalatestplus"    %% "scalatestplus-scalacheck" % scalatestplusScalacheckVersion,
    "com.danielasfregola"  %% "random-data-generator"    % randomDataGeneratorVersion,
    "io.github.wolfendale" %% "scalacheck-gen-regexp"    % scalacheckGenRegexpVersion
  ).map(_ % Test)

  val it: Seq[ModuleID] = Seq(
    "uk.gov.hmrc" %% "bootstrap-test-play-30" % bootstrapVersion % Test
  )

  def apply(): Seq[ModuleID] = compile ++ test
}
