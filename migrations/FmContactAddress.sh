#!/bin/bash

echo ""
echo "Applying migration FmContactAddress"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /fmContactAddress                        controllers.FmContactAddressController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /fmContactAddress                        controllers.FmContactAddressController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /change-FmContactAddress                  controllers.FmContactAddressController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /change-FmContactAddress                  controllers.FmContactAddressController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "fmContactAddress.title = fmContactAddress" >> ../conf/messages.en
echo "fmContactAddress.heading = fmContactAddress" >> ../conf/messages.en
echo "fmContactAddress.checkYourAnswersLabel = fmContactAddress" >> ../conf/messages.en
echo "fmContactAddress.error.required = Enter fmContactAddress" >> ../conf/messages.en
echo "fmContactAddress.error.length = FmContactAddress must be 200 characters or less" >> ../conf/messages.en
echo "fmContactAddress.change.hidden = FmContactAddress" >> ../conf/messages.en


echo "Adding to ViewInstances"
awk '/trait ViewInstances/ {\
    print;\
    print "";\
    print "   val viewFmContactAddress: FmContactAddressView =";\
    print "    new FmContactAddressView(pillar2layout, formWithCSRF, govukErrorSummary, govukInput, govukButton)";\
    next }1' ../test/helpers/ViewInstances.scala > tmp && mv tmp  ../test/helpers/ViewInstances.scala

echo "Migration FmContactAddress completed"
