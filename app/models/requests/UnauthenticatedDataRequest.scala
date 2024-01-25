package models.requests

import models.UserAnswers
import play.api.mvc.{Request, WrappedRequest}

case class UnauthenticatedDataRequest[A](
                                          request: Request[A],
                                          userId: String,
                                          userAnswers: UserAnswers
                                        ) extends WrappedRequest[A](request)
