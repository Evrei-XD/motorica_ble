/*
 * Copyright (C) 2016 skydoves
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.skydoves.waterdays.consts

/**
 * Developed by skydoves on 2017-08-18.
 * Copyright (c) 2017 skydoves rights reserved.
 */

object LocalNames {
  fun getLocalName(index: Int): String {
    when (index) {
      1 -> return "русский"
      2 -> return "не русский"
      3 -> return "англыйский"
      4 -> return "таджикский"
      5 -> return "индийский"
      6 -> return "японский"
      7 -> return "китайский"
      8 -> return "русский2"
      9 -> return "русский3"
      10 -> return "русский4"
      11 -> return "русский5"
      12 -> return "русский6"
      13 -> return "русский7"
      14 -> return "русский8"
      15 -> return "русский9"
      else -> return "русский10"
    }
  }
}
