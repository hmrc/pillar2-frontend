package views.rfm

import base.ViewSpecBase
import org.jsoup.Jsoup
import views.html.rfm.StartPageView

class StartPageViewSpec extends ViewSpecBase {

  val page = inject[StartPageView]

  val view = Jsoup.parse(page()(request, appConfig, messages).toString())

  "Start Page View" should {

    "have a title" in {
      view.getElementsByTag("title").text must include(messages("rfm.startPage.title"))
    }

    "have a caption" in {
      view.getElementsByClass("govuk-caption-l").text must include(messages("rfm.heading.caption"))
    }

    "have a heading" in {
      view.getElementsByTag("h1").text must include(messages("rfm.startPage.heading"))
    }

    "have a sub heading" in {
      view.getElementsByClass("govuk-heading-m").get(0).text must include(messages("rfm.startPage.subHeading1"))
      view.getElementsByClass("govuk-heading-m").get(1).text must include(messages("rfm.startPage.subHeading2"))
      view.getElementsByClass("govuk-heading-m").get(2).text must include(messages("rfm.startPage.subHeading3"))
      view.getElementsByClass("govuk-heading-m").get(3).text must include(messages("rfm.startPage.subHeading4"))
    }

    "have a legend" in {
      view.getElementsByClass("govuk-heading-s").get(0).text must include(messages("rfm.startPage.legend"))
    }

    "have a paragraph body" in {
      view.getElementsByClass("govuk-body").get(0).text must include(messages("rfm.startPage.p1"))
      view.getElementsByClass("govuk-body").get(1).text must include(messages("rfm.startPage.p2"))
      view.getElementsByClass("govuk-body").get(2).text must include(
        messages("rfm.startPage.p3")
          .replace("{0}", messages("rfm.startPage.p3.link") + ".")
      )
      view.getElementsByClass("govuk-body").get(3).text must include(messages("rfm.startPage.p4"))
      view.getElementsByClass("govuk-body").get(4).text must include(messages("rfm.startPage.p5"))
      view.getElementsByClass("govuk-body").get(5).text must include(messages("rfm.startPage.p6"))
      view.getElementsByClass("govuk-body").get(6).text must include(messages("rfm.startPage.p7"))
      view.getElementsByClass("govuk-body").get(7).text must include(messages("rfm.startPage.p8"))
    }

    "have a bulleted list" in {
      view.getElementsByTag("li").get(0).text  must include(messages("rfm.startPage.b1"))
      view.getElementsByTag("li").get(1).text  must include(messages("rfm.startPage.b2"))
      view.getElementsByTag("li").get(2).text  must include(messages("rfm.startPage.b3"))
      view.getElementsByTag("li").get(3).text  must include(messages("rfm.startPage.b4"))
      view.getElementsByTag("li").get(4).text  must include(messages("rfm.startPage.b5"))
      view.getElementsByTag("li").get(5).text  must include(messages("rfm.startPage.b6"))
      view.getElementsByTag("li").get(6).text  must include(messages("rfm.startPage.b7"))
      view.getElementsByTag("li").get(7).text  must include(messages("rfm.startPage.b8"))
      view.getElementsByTag("li").get(8).text  must include(messages("rfm.startPage.b9"))
      view.getElementsByTag("li").get(9).text  must include(messages("rfm.startPage.b10"))
      view.getElementsByTag("li").get(10).text must include(messages("rfm.startPage.b11"))
      view.getElementsByTag("li").get(11).text must include(messages("rfm.startPage.b12"))
    }

    "have a button" in {
      view.getElementsByClass("govuk-button").text must include(messages("site.confirm-and-continue"))
    }
  }
}
