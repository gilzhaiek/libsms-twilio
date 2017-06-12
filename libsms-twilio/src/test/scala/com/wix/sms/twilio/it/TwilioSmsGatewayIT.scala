package com.wix.sms.twilio.it

import com.google.api.client.http.javanet.NetHttpTransport
import com.wix.sms.SmsErrorException
import com.wix.sms.model.{Sender, SmsGateway}
import com.wix.sms.twilio.testkit.TwilioDriver
import com.wix.sms.twilio.{Credentials, TwilioSmsGateway}
import org.specs2.mutable.SpecWithJUnit
import org.specs2.specification.Scope

class TwilioSmsGatewayIT extends SpecWithJUnit {
  private val twilioPort = 10008

  val driver = new TwilioDriver(port = twilioPort)
  step {
    driver.start()
  }

  sequential

  trait Ctx extends Scope {
    val requestFactory = new NetHttpTransport().createRequestFactory()

    val someCredentials = Credentials(
      accountSid = "someAccountSid",
      authToken = "someAuthToken"
    )
    val someDestPhone = "+12125551234"
    val someSender = Sender(
      phone = Some("+12125554321")
    )
    val somePlainText = "some plain text"
    val someUnicodeText = "some יוניקוד text"

    val someMessageId = "someMessageId"

    val twilio: SmsGateway = new TwilioSmsGateway(
      requestFactory = requestFactory,
      endpoint = s"http://localhost:$twilioPort/",
      credentials = someCredentials
    )

    driver.reset()
  }

  "sendPlain" should {
    "successfully yield a message ID on valid request" in new Ctx {
      driver.aSendMessageFor(
        credentials = someCredentials,
        sender = someSender,
        destPhone = someDestPhone,
        text = somePlainText
      ) returns(
        msgId = someMessageId
      )

      twilio.sendPlain(
        sender = someSender,
        destPhone = someDestPhone,
        text = somePlainText
      ) must beASuccessfulTry(
        check = ===(someMessageId)
      )
    }

    "gracefully fail on error" in new Ctx {
      val someCode = "some code"
      val someMessage = "some message"

      driver.aSendMessageFor(
        credentials = someCredentials,
        sender = someSender,
        destPhone = someDestPhone,
        text = somePlainText
      ) failsWith(
        code = someCode,
        message = someMessage
      )

      twilio.sendPlain(
        sender = someSender,
        destPhone = someDestPhone,
        text = somePlainText
      ) must beAFailedTry.like {
        case e: SmsErrorException => e.message must (contain(someCode) and contain(someMessage))
      }
    }

    "gracefully fail on blacklist" in new Ctx {
      driver.aSendMessageFor(
        credentials = someCredentials,
        sender = someSender,
        destPhone = someDestPhone,
        text = somePlainText
      ) failsDueToBlacklist()

      twilio.sendPlain(
        sender = someSender,
        destPhone = someDestPhone,
        text = somePlainText
      ) must beAFailedTry(
        check = beAnInstanceOf[SmsErrorException]
      )
    }
  }

  "sendUnicode" should {
    "successfully yield a message ID on valid request" in new Ctx {
      driver.aSendMessageFor(
        credentials = someCredentials,
        sender = someSender,
        destPhone = someDestPhone,
        text = someUnicodeText
      ) returns(
        msgId = someMessageId
      )

      twilio.sendUnicode(
        sender = someSender,
        destPhone = someDestPhone,
        text = someUnicodeText
      ) must beASuccessfulTry(
        check = ===(someMessageId)
      )
    }

    "gracefully fail on error" in new Ctx {
      val someCode = "some code"
      val someMessage = "some message"

      driver.aSendMessageFor(
        credentials = someCredentials,
        sender = someSender,
        destPhone = someDestPhone,
        text = someUnicodeText
      ) failsWith(
        code = someCode,
        message = someMessage
      )

      twilio.sendUnicode(
        sender = someSender,
        destPhone = someDestPhone,
        text = someUnicodeText
      ) must beAFailedTry.like {
        case e: SmsErrorException => e.message must (contain(someCode) and contain(someMessage))
      }
    }

    "gracefully fail on blacklist" in new Ctx {
      driver.aSendMessageFor(
        credentials = someCredentials,
        sender = someSender,
        destPhone = someDestPhone,
        text = somePlainText
      ) failsDueToBlacklist()

      twilio.sendUnicode(
        sender = someSender,
        destPhone = someDestPhone,
        text = somePlainText
      ) must beAFailedTry(
        check = beAnInstanceOf[SmsErrorException]
      )
    }
  }

  step {
    driver.stop()
  }
}
