package views.rfm

import base.ViewSpecBase
import org.jsoup.Jsoup
import views.html.rfm.StartPageView

class StartPageViewSpec extends ViewSpecBase {

  val page = inject[StartPageView]

  val view = Jsoup.parse(page()(request, appConfig, messages).toString())

  "Start Page View" should {

    "have a title" in {
      view.getElementsByTag("title").text must include("Replace the filing member for a Pillar 2 top-up taxes account")
    }

    "have a caption" in {
      view.getElementsByClass("govuk-caption-l").text must include("Replace filing member")
    }

    "have a heading" in {
      view.getElementsByTag("h1").text must include("Replace the filing member for a Pillar 2 top-up taxes account")
    }

    "have a sub heading" in {
      view.getElementsByClass("govuk-heading-m").get(0).text must include(
        "Tell HMRC when you have replaced your " +
          "filing member"
      )
      view.getElementsByClass("govuk-heading-m").get(1).text must include("Who can replace a filing member")
      view.getElementsByClass("govuk-heading-m").get(2).text must include("Obligations as the filing member")
      view.getElementsByClass("govuk-heading-m").get(3).text must include("What you will need")
    }

    "have a legend" in {
      view.getElementsByClass("govuk-heading-s").get(0).text must include(
        "By continuing you confirm you are able to " +
          "act as a new filing member for your group"
      )
    }

    "have a paragraph body" in {
      view.getElementsByClass("govuk-body").get(0).text must include(
        "Use this service to replace the filing member " +
          "for an existing Pillar 2 top-up taxes account."
      )

      view.getElementsByClass("govuk-body").get(1).text must include(
        "You must replace your filing member’s details " +
          "within 6 months of the change occurring in your group."
      )

      view.getElementsByClass("govuk-body").get(2).text must include(
        "If your group has not yet registered, you will " +
          "need to register to report Pillar 2 top-up taxes. You can choose to nominate a filing member during registration."
      )

      view.getElementsByClass("govuk-body").get(3).text must include("The filing member can be replaced by:")

      view.getElementsByClass("govuk-body").get(4).text must include(
        "As the new filing member, you will take over " +
          "the obligations to:"
      )

      view.getElementsByClass("govuk-body").get(5).text must include(
        "If you fail to meet your obligations as a " +
          "filing member, you may be liable for penalties."
      )

      view.getElementsByClass("govuk-body").get(6).text must include(
        "This section has force of law under paragraph " +
          "8 of Schedule 14 to the Finance (No. 2) Act 2023."
      )

      view.getElementsByClass("govuk-body").get(7).text must include(
        "You will need the following information to " +
          "replace the filing member:"
      )
    }

    "have a bulleted list" in {
      view.getElementsByTag("li").get(0).text must include(
        "the ultimate parent (UPE), taking over from a nominated " +
          "filing member as the group’s default filing member"
      )

      view.getElementsByTag("li").get(1).text must include(
        "a new nominated filing member, taking over from a " +
          "previous filing member"
      )

      view.getElementsByTag("li").get(2).text must include("act as HMRC’s primary contact in relation to the group’s Pillar 2 top-up tax compliance")

      view.getElementsByTag("li").get(3).text must include("submit your group’s Pillar 2 top-up tax returns")

      view.getElementsByTag("li").get(4).text must include(
        "ensure your group’s Pillar 2 top-up taxes account " +
          "accurately reflects their records"
      )

      view.getElementsByTag("li").get(5).text must include(
        "the Government Gateway user ID for the new " +
          "filing member who is replacing the current filing member"
      )

      view.getElementsByTag("li").get(6).text must include("the group’s Pillar 2 top-up taxes ID")

      view.getElementsByTag("li").get(7).text must include(
        "the date the group first registered to report their " +
          "Pillar 2 top up taxes in the UK"
      )

      view.getElementsByTag("li").get(8).text must include(
        "for a UK registered new filing member, you will need " +
          "the company registration number (CRN) and Unique Taxpayer Reference (UTR) and their postcode"
      )

      view.getElementsByTag("li").get(9).text must include(
        "for an overseas new filing member, you will need to give " +
          "their name and a registered address"
      )

      view.getElementsByTag("li").get(10).text must include(
        "contact preferences and details for up to 2 individuals " +
          "or teams that we can use as primary contacts for Pillar 2 compliance"
      )

      view.getElementsByTag("li").get(11).text must include(
        "a contact address for the group that we can send " +
          "correspondence to"
      )
    }

    "have a button" in {
      view.getElementsByClass("govuk-button").text must include("Confirm and continue")
    }
  }
}
