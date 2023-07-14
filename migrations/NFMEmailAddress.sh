#!/bin/bash

echo ""
echo "Applying migration NFMEmailAddress"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /nFMEmailAddress                        controllers.NFMEmailAddressController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /nFMEmailAddress                        controllers.NFMEmailAddressController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changeNFMEmailAddress                  controllers.NFMEmailAddressController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changeNFMEmailAddress                  controllers.NFMEmailAddressController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "nFMEmailAddress.title = nFMEmailAddress" >> ../conf/messages.en
echo "nFMEmailAddress.heading = nFMEmailAddress" >> ../conf/messages.en
echo "nFMEmailAddress.checkYourAnswersLabel = nFMEmailAddress" >> ../conf/messages.en
echo "nFMEmailAddress.error.required = Enter nFMEmailAddress" >> ../conf/messages.en
echo "nFMEmailAddress.error.length = NFMEmailAddress must be 200 characters or less" >> ../conf/messages.en
echo "nFMEmailAddress.change.hidden = NFMEmailAddress" >> ../conf/messages.en


echo "Adding to ViewInstances"
awk '/trait ViewInstances/ {\
    print;\
    print "";\
    print "   val viewNFMEmailAddress: NFMEmailAddressView =";\
    print "    new NFMEmailAddressView(pillar2layout, formWithCSRF, govukErrorSummary, govukInput, govukButton)";\
    next }1' ../test/helpers/ViewInstances.scala > tmp && mv tmp  ../test/helpers/ViewInstances.scala

echo "Migration NFMEmailAddress completed"
