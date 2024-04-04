#!/bin/bash

echo ""
echo "Applying migration rfm/RfmFinalCheckYourAnswers"

echo "Adding routes to conf/app.routes"
echo "" >> ../conf/app.routes
echo "GET        /rfm/RfmFinalCheckYourAnswers                       controllers.rfm/RfmFinalCheckYourAnswersController.onPageLoad()" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "rfm/RfmFinalCheckYourAnswers.title = rfm/RfmFinalCheckYourAnswers" >> ../conf/messages.en
echo "rfm/RfmFinalCheckYourAnswers.heading = rfm/RfmFinalCheckYourAnswers" >> ../conf/messages.en

echo "Migration rfm/RfmFinalCheckYourAnswers completed"
