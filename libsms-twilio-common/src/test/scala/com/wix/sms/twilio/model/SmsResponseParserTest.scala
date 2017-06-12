package com.wix.sms.twilio.model

import org.specs2.mutable.SpecWithJUnit
import org.specs2.specification.Scope

class SmsResponseParserTest extends SpecWithJUnit {
  trait Ctx extends Scope {
    val someSmsResponse = SmsResponse(
      sid = Some("some sid")
    )
  }

  "stringify and then parse" should {
    "yield an object similar to the original one" in new Ctx {
      val json = SmsResponseParser.stringify(someSmsResponse)
      SmsResponseParser.parse(json) must beEqualTo(someSmsResponse)
    }
  }
}
