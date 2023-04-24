#!/bin/bash

echo ""
echo "Applying migration TradingBusinessConfirmation"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /tradingBusinessConfirmation                        controllers.TradingBusinessConfirmationController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /tradingBusinessConfirmation                        controllers.TradingBusinessConfirmationController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changeTradingBusinessConfirmation                  controllers.TradingBusinessConfirmationController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changeTradingBusinessConfirmation                  controllers.TradingBusinessConfirmationController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "tradingBusinessConfirmation.title = tradingBusinessConfirmation" >> ../conf/messages.en
echo "tradingBusinessConfirmation.heading = tradingBusinessConfirmation" >> ../conf/messages.en
echo "tradingBusinessConfirmation.yes = Yes" >> ../conf/messages.en
echo "tradingBusinessConfirmation.no = No" >> ../conf/messages.en
echo "tradingBusinessConfirmation.checkYourAnswersLabel = tradingBusinessConfirmation" >> ../conf/messages.en
echo "tradingBusinessConfirmation.error.required = Select tradingBusinessConfirmation" >> ../conf/messages.en
echo "tradingBusinessConfirmation.change.hidden = TradingBusinessConfirmation" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/trait UserAnswersEntryGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryTradingBusinessConfirmationUserAnswersEntry: Arbitrary[(TradingBusinessConfirmationPage.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[TradingBusinessConfirmationPage.type]";\
    print "        value <- arbitrary[TradingBusinessConfirmation].map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test-utils/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test-utils/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryTradingBusinessConfirmationPage: Arbitrary[TradingBusinessConfirmationPage.type] =";\
    print "    Arbitrary(TradingBusinessConfirmationPage)";\
    next }1' ../test-utils/generators/PageGenerators.scala > tmp && mv tmp ../test-utils/generators/PageGenerators.scala

echo "Adding to ModelGenerators"
awk '/trait ModelGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryTradingBusinessConfirmation: Arbitrary[TradingBusinessConfirmation] =";\
    print "    Arbitrary {";\
    print "      Gen.oneOf(TradingBusinessConfirmation.values.toSeq)";\
    print "    }";\
    next }1' ../test-utils/generators/ModelGenerators.scala > tmp && mv tmp ../test-utils/generators/ModelGenerators.scala

echo "Adding to UserAnswersGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[(TradingBusinessConfirmationPage.type, JsValue)] ::";\
    next }1' ../test-utils/generators/UserAnswersGenerator.scala > tmp && mv tmp ../test-utils/generators/UserAnswersGenerator.scala

echo "Migration TradingBusinessConfirmation completed"
