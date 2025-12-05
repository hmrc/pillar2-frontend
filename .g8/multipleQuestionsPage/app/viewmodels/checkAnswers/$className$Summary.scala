package viewmodels.checkAnswers

import controllers.routes
import models.{CheckMode, UserAnswers}
import pages.$className$Page
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import scala.language.implicitConversions
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist.*
import viewmodels.implicits.given

object $className$Summary  {

  def row(answers: UserAnswers)(using messages: Messages): Option[SummaryListRow] =
    answers.get($className$Page).map {
      answer =>

      val value = HtmlFormat.escape(answer.$field1Name$).toString + "<br/>" + HtmlFormat.escape(answer.$field2Name$).toString

        SummaryListRowViewModel(
          key     = "$className;format="decap"$.checkYourAnswersLabel",
          value   = ValueViewModel(HtmlContent(value)),
          actions = Seq(
            ActionItemViewModel("site.change", routes.$className$Controller.onPageLoad(CheckMode).url)
              .withVisuallyHiddenText(messages("$className;format="decap"$.change.hidden"))
          )
        )
    }
}
