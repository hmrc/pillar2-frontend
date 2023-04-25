#!/bin/bash

echo ""
echo "Applying migration GroupTerritories"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /GroupTerritories                                   controllers.GroupTerritoriesController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /GroupTerritories                                   controllers.GroupTerritoriesController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changeGroupTerritories                             controllers.GroupTerritoriesController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changeGroupTerritories                             controllers.GroupTerritoriesController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "groupTerritories.title = groupTerritories" >> ../conf/messages.en
echo "groupTerritories.heading = groupTerritories" >> ../conf/messages.en
echo "groupTerritories.yes = Yes" >> ../conf/messages.en
echo "groupTerritories.no = No" >> ../conf/messages.en
echo "groupTerritories.checkYourAnswersLabel = groupTerritories" >> ../conf/messages.en
echo "groupTerritories.error.required = Select groupTerritories" >> ../conf/messages.en
echo "groupTerritories.change.hidden = GroupTerritories" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/trait UserAnswersEntryGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryGroupTerritoriesUserAnswersEntry: Arbitrary[(GroupTerritoriesPage.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[GroupTerritoriesPage.type]";\
    print "        value <- arbitrary[GroupTerritories].map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test-utils/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test-utils/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryGroupTerritoriesPage: Arbitrary[GroupTerritoriesPage.type] =";\
    print "    Arbitrary(GroupTerritoriesPage)";\
    next }1' ../test-utils/generators/PageGenerators.scala > tmp && mv tmp ../test-utils/generators/PageGenerators.scala

echo "Adding to ModelGenerators"
awk '/trait ModelGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryGroupTerritories: Arbitrary[GroupTerritories] =";\
    print "    Arbitrary {";\
    print "      Gen.oneOf(GroupTerritories.values.toSeq)";\
    print "    }";\
    next }1' ../test-utils/generators/ModelGenerators.scala > tmp && mv tmp ../test-utils/generators/ModelGenerators.scala

echo "Adding to UserAnswersGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[(GroupTerritoriesPage.type, JsValue)] ::";\
    next }1' ../test-utils/generators/UserAnswersGenerator.scala > tmp && mv tmp ../test-utils/generators/UserAnswersGenerator.scala

echo "Migration GroupTerritories completed"
