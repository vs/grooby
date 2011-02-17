package org.tmatesoft.grooby.exec

interface IEsCommandOutputHandler {

  Object handleOutput(String out, String err)
}
