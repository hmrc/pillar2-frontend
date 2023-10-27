/*
 * Copyright 2023 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package controllers

import com.google.inject.Inject
import config.FrontendAppConfig
import connectors.UserAnswersConnectors
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import helpers.SubscriptionHelpers
import models.fm.{FilingMember, FilingMemberNonUKData}
import models.registration.{Registration, WithoutIdRegData}
import models.requests.DataRequest
import pages._
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.{RegisterWithoutIdService, SubscriptionService, TaxEnrolmentService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.RowStatus
import utils.countryOptions.CountryOptions
import viewmodels.checkAnswers._
import viewmodels.govuk.summarylist._
import views.html.CheckYourAnswersView

import scala.concurrent.{ExecutionContext, Future}

class CheckYourAnswersController @Inject() (
  override val messagesApi:              MessagesApi,
  identify:                              IdentifierAction,
  getData:                               DataRetrievalAction,
  requireData:                           DataRequiredAction,
  override val registerWithoutIdService: RegisterWithoutIdService,
  override val subscriptionService:      SubscriptionService,
  override val userAnswersConnectors:    UserAnswersConnectors,
  override val taxEnrolmentService:      TaxEnrolmentService,
  val controllerComponents:              MessagesControllerComponents,
  view:                                  CheckYourAnswersView,
  countryOptions:                        CountryOptions
)(implicit ec:                           ExecutionContext, appConfig: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport
    with RegisterAndSubscribe
    with Logging
    with SubscriptionHelpers {

  // noinspection ScalaStyle
  def onPageLoad(): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    val groupDetailList = SummaryListViewModel(
      rows = Seq(
        MneOrDomesticSummary.row(request.userAnswers),
        GroupAccountingPeriodSummary.row(request.userAnswers),
        GroupAccountingPeriodStartDateSummary.row(request.userAnswers),
        GroupAccountingPeriodEndDateSummary.row(request.userAnswers)
      ).flatten
    )
    val upeSummaryList = SummaryListViewModel(
      rows = Seq(
        UpeNameRegistrationSummary.row(request.userAnswers),
        UpeRegisteredAddressSummary.row(request.userAnswers, countryOptions),
        UpeContactNameSummary.row(request.userAnswers),
        UpeContactEmailSummary.row(request.userAnswers),
        UpeTelephonePreferenceSummary.row(request.userAnswers),
        UPEContactTelephoneSummary.row(request.userAnswers),
        EntityTypeIncorporatedCompanyNameUpeSummary.row(request.userAnswers),
        EntityTypeIncorporatedCompanyRegUpeSummary.row(request.userAnswers),
        EntityTypeIncorporatedCompanyUtrUpeSummary.row(request.userAnswers),
        EntityTypePartnershipCompanyNameUpeSummary.row(request.userAnswers),
        EntityTypePartnershipCompanyRegUpeSummary.row(request.userAnswers),
        EntityTypePartnershipCompanyUtrUpeSummary.row(request.userAnswers)
      ).flatten
    )
    val nfmSummaryList = SummaryListViewModel(
      rows = Seq(
        NominateFilingMemberYesNoSummary.row(request.userAnswers),
        NfmNameRegistrationSummary.row(request.userAnswers),
        NfmRegisteredAddressSummary.row(request.userAnswers, countryOptions),
        NfmContactNameSummary.row(request.userAnswers),
        NfmEmailAddressSummary.row(request.userAnswers),
        NfmTelephonePreferenceSummary.row(request.userAnswers),
        NfmContactTelephoneSummary.row(request.userAnswers),
        EntityTypeIncorporatedCompanyNameNfmSummary.row(request.userAnswers),
        EntityTypeIncorporatedCompanyRegNfmSummary.row(request.userAnswers),
        EntityTypeIncorporatedCompanyUtrNfmSummary.row(request.userAnswers),
        EntityTypePartnershipCompanyNameNfmSummary.row(request.userAnswers),
        EntityTypePartnershipCompanyRegNfmSummary.row(request.userAnswers),
        EntityTypePartnershipCompanyUtrNfmSummary.row(request.userAnswers)
      ).flatten
    )
    val primaryContactList = SummaryListViewModel(
      rows = Seq(
        ContactNameComplianceSummary.row(request.userAnswers),
        ContactEmailAddressSummary.row(request.userAnswers),
        ContactByTelephoneSummary.row(request.userAnswers),
        ContactCaptureTelephoneDetailsSummary.row(request.userAnswers)
      ).flatten
    )
    val secondaryPreference = SummaryListViewModel(
      rows = Seq(AddSecondaryContactSummary.row(request.userAnswers)).flatten
    )
    val secondaryContactList = SummaryListViewModel(
      rows = Seq(
        SecondaryContactNameSummary.row(request.userAnswers),
        SecondaryContactEmailSummary.row(request.userAnswers),
        SecondaryTelephonePreferenceSummary.row(request.userAnswers),
        SecondaryTelephoneSummary.row(request.userAnswers)
      ).flatten
    )
    val address = SummaryListViewModel(
      rows = Seq(ContactCorrespondenceAddressSummary.row(request.userAnswers, countryOptions)).flatten
    )

    Ok(view(upeSummaryList, nfmSummaryList, groupDetailList, primaryContactList, secondaryPreference, secondaryContactList, address))

  }

  def onSubmit(): Action[AnyContent] = (identify andThen getData andThen requireData) async { implicit request =>
    val upe = createUltimateParent(request)
    val nfm = createFilingMember(request)
    (upe, nfm) match {
      case (Right(upe), Right(fm)) => createRegistrationAndSubscription(upe, fm)
      case _                       => Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
    }
  }
  // noinspection ScalaStyle
  private def createFilingMember(request: DataRequest[AnyContent]): Either[Result, FilingMember] =
    request.userAnswers
      .get(NominateFilingMemberPage)
      .map { nominated =>
        if (nominated) {
          request.userAnswers
            .get(fmRegisteredInUKPage)
            .map { ukBased =>
              if (ukBased) {
                (for {
                  entityType <- request.userAnswers.get(fmEntityTypePage)
                  grsData    <- request.userAnswers.get(fmGRSResponsePage)
                } yield Right(
                  FilingMember(isNfmRegisteredInUK = ukBased, orgType = Some(entityType), withIdRegData = Some(grsData), withoutIdRegData = None)
                )).getOrElse(Left(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())))
              } else if (!ukBased) {
                (for {
                  nameReg      <- request.userAnswers.get(fmNameRegistrationPage)
                  address      <- request.userAnswers.get(fmRegisteredAddressPage)
                  contactName  <- request.userAnswers.get(fmContactNamePage)
                  contactEmail <- request.userAnswers.get(fmContactEmailPage)
                  phonePref    <- request.userAnswers.get(fmPhonePreferencePage)
                } yield
                  if (phonePref) {
                    request.userAnswers
                      .get(fmCapturePhonePage)
                      .map { tel =>
                        Right(
                          FilingMember(
                            isNfmRegisteredInUK = false,
                            orgType = None,
                            withIdRegData = None,
                            withoutIdRegData = Some(
                              FilingMemberNonUKData(
                                registeredFmName = nameReg,
                                registeredFmAddress = address,
                                contactName = contactName,
                                emailAddress = contactEmail,
                                phonePreference = phonePref,
                                telephone = Some(tel)
                              )
                            )
                          )
                        )
                      // here we could send them to a page where they get told you haven't answered every question try again
                      }
                      .getOrElse(Left(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())))
                  } else {
                    Right(
                      FilingMember(
                        isNfmRegisteredInUK = false,
                        orgType = None,
                        withIdRegData = None,
                        withoutIdRegData = Some(
                          FilingMemberNonUKData(
                            registeredFmName = nameReg,
                            registeredFmAddress = address,
                            contactName = contactName,
                            emailAddress = contactEmail,
                            phonePreference = phonePref,
                            telephone = None
                          )
                        )
                      )
                    )
                  }).getOrElse(Left(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())))
              } else {
                Left(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
              }
            }
            .getOrElse(Left(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())))
        } else if (!nominated) {
          //what will you do if a filing member is not nominated?
          Left(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
        } else {
          Left(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
        }
      }
      .getOrElse(Left(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())))

  // noinspection ScalaStyle
  private def createUltimateParent(request: DataRequest[AnyContent]): Either[Result, Registration] =
    request.userAnswers
      .get(upeRegisteredInUKPage)
      .map { ukBased =>
        if (ukBased) {
          (for {
            entityType <- request.userAnswers.get(upeEntityTypePage)
            grsData    <- request.userAnswers.get(upeGRSResponsePage)
          } yield Right(
            Registration(isUPERegisteredInUK = ukBased, orgType = Some(entityType), withIdRegData = Some(grsData), withoutIdRegData = None)
          )).getOrElse(Left(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())))
        } else if (!ukBased) {
          (for {
            nameReg      <- request.userAnswers.get(upeNameRegistrationPage)
            address      <- request.userAnswers.get(upeRegisteredAddressPage)
            contactName  <- request.userAnswers.get(upeContactNamePage)
            contactEmail <- request.userAnswers.get(upeContactEmailPage)
            phonePref    <- request.userAnswers.get(upePhonePreferencePage)
          } yield
            if (phonePref) {
              request.userAnswers
                .get(upeCapturePhonePage)
                .map { tel =>
                  Right(
                    Registration(
                      isUPERegisteredInUK = false,
                      orgType = None,
                      withIdRegData = None,
                      withoutIdRegData = Some(
                        WithoutIdRegData(
                          upeNameRegistration = nameReg,
                          upeRegisteredAddress = address,
                          upeContactName = contactName,
                          emailAddress = contactEmail,
                          contactUpeByTelephone = phonePref,
                          telephoneNumber = Some(tel)
                        )
                      )
                    )
                  )
                // here we could send them to a page where they get told you haven't answered every question try again
                }
                .getOrElse(Left(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())))
            } else {
              Right(
                Registration(
                  isUPERegisteredInUK = false,
                  orgType = None,
                  withIdRegData = None,
                  withoutIdRegData = Some(
                    WithoutIdRegData(
                      upeNameRegistration = nameReg,
                      upeRegisteredAddress = address,
                      upeContactName = contactName,
                      emailAddress = contactEmail,
                      contactUpeByTelephone = phonePref,
                      telephoneNumber = None
                    )
                  )
                )
              )
            }).getOrElse(Left(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())))
        } else {
          Left(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
        }

      }
      .getOrElse(Left(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())))
}
