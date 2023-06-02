#!/bin/bash

echo ""
echo "Applying migration UpeNameRegistration"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /upeNameRegistration                        controllers.UpeNameRegistrationController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /upeNameRegistration                        controllers.UpeNameRegistrationController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changeUpeNameRegistration                  controllers.UpeNameRegistrationController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changeUpeNameRegistration                  controllers.UpeNameRegistrationController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "upeNameRegistration.title = upeNameRegistration" >> ../conf/messages.en
echo "upeNameRegistration.heading = upeNameRegistration" >> ../conf/messages.en
echo "upeNameRegistration.checkYourAnswersLabel = upeNameRegistration" >> ../conf/messages.en
echo "upeNameRegistration.error.required = Enter upeNameRegistration" >> ../conf/messages.en
echo "upeNameRegistration.error.length = UpeNameRegistration must be 200 characters or less" >> ../conf/messages.en
echo "upeNameRegistration.change.hidden = UpeNameRegistration" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/trait UserAnswersEntryGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryUpeNameRegistrationUserAnswersEntry: Arbitrary[(UpeNameRegistrationPage.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[UpeNameRegistrationPage.type]";\
    print "        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test-utils/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test-utils/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryUpeNameRegistrationPage: Arbitrary[UpeNameRegistrationPage.type] =";\
    print "    Arbitrary(UpeNameRegistrationPage)";\
    next }1' ../test-utils/generators/PageGenerators.scala > tmp && mv tmp ../test-utils/generators/PageGenerators.scala

echo "Adding to UserAnswersGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[(UpeNameRegistrationPage.type, JsValue)] ::";\
    next }1' ../test-utils/generators/UserAnswersGenerator.scala > tmp && mv tmp ../test-utils/generators/UserAnswersGenerator.scala

echo "Migration UpeNameRegistration completed"
