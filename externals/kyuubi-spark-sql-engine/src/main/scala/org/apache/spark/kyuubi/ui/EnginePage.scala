/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.spark.kyuubi.ui

import java.util.Date
import javax.servlet.http.HttpServletRequest

import scala.xml.Node

import org.apache.spark.ui.{UIUtils, WebUIPage}
import org.apache.spark.ui.UIUtils.formatDurationVerbose

case class EnginePage(parent: EngineTab) extends WebUIPage("") {
  override def render(request: HttpServletRequest): Seq[Node] = {
    val content =
      generateBasicStats() ++
      <br/> ++
      stop(request) ++
      <br/> ++
      <h4>
        {parent.engine.backendService.sessionManager.getOpenSessionCount} session(s) are online,
        running {parent.engine.backendService.sessionManager.operationManager.getOperationCount}
        operations
      </h4>
    UIUtils.headerSparkPage(request, parent.name, content, parent)
  }

  private def generateBasicStats(): Seq[Node] = {
    val timeSinceStart = System.currentTimeMillis() - parent.engine.getStartTime
    <ul class ="list-unstyled">
      <li>
        <strong>Started at: </strong>
        {new Date(parent.engine.getStartTime)}
      </li>
      <li>
        <strong>Latest Logout at: </strong>
        {new Date(parent.engine.backendService.sessionManager.latestLogoutTime)}
      </li>
      <li>
        <strong>Time since start: </strong>
        {formatDurationVerbose(timeSinceStart)}
      </li>
      <li>
        <strong>Background execution pool threads alive: </strong>
        {parent.engine.backendService.sessionManager.getExecPoolSize}
      </li>
      <li>
        <strong>Background execution pool threads active: </strong>
        {parent.engine.backendService.sessionManager.getActiveCount}
      </li>
    </ul>
  }

  private def stop(request: HttpServletRequest): Seq[Node] = {
    val basePath = UIUtils.prependBaseUri(request, parent.basePath)
    if (parent.killEnabled) {
      val confirm =
        s"if (window.confirm('Are you sure you want to kill kyuubi engine ?')) " +
          "{ this.parentNode.submit(); return true; } else { return false; }"
      val stopLinkUri = s"$basePath/kyuubi/stop"
      <ul class ="list-unstyled">
        <li>
          <strong>Stop kyuubi engine:  </strong>
          <a href={stopLinkUri} onclick={confirm} class="stop-link">(kill)</a>
        </li>
      </ul>
    } else {
      Seq.empty
    }
  }
}
