package pl.stswn.tapir

import zio.Has

package object server {
  type Logic = Has[Logic.Service]
}
