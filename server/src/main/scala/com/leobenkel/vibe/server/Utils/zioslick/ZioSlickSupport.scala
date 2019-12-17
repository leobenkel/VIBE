/*
 * Copyright 2019 Roberto Leibman
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

package com.leobenkel.vibe.server.Utils.zioslick

import java.sql.SQLException

import com.leobenkel.vibe.server.Utils.zioslick.zioslick.SlickZIO
import slick.SlickException
import slick.dbio._
import zio._

import scala.language.implicitConversions

trait ZioSlickSupport {
  implicit def fromDBIO[R](dbio: DBIO[R]): SlickZIO[R] =
    for {
      db <- ZIO.accessM[DatabaseProvider](_.databaseProvider.db)
      r <- ZIO
        .fromFuture(_ => db.run(dbio))
        .mapError {
          case e: SlickException => RepositoryException("Slick Repository Error", Some(e))
          case e: SQLException   => RepositoryException("SQL Repository Error", Some(e))
        }
    } yield r

  def from[R](zio: IO[Throwable, R])(): DBIOAction[R, NoStream, Effect] = {
    val runtime = new DefaultRuntime {}
    DBIO.from(runtime.unsafeRunToFuture(zio))
  }

}
