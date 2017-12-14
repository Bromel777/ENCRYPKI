package encry.nodeView

import encry.modifiers.EncryPersistentModifier
import encry.modifiers.mempool.EncryBaseTransaction
import encry.modifiers.mempool.box.body.BaseBoxBody
import encry.modifiers.mempool.box.{EncryBaseBox, EncryBoxStateChanges}
import scorex.core.transaction.box.proposition.Proposition
import scorex.core.transaction.state.MinimalState
import scorex.core.utils.ScorexLogging
import scorex.core.{EphemerealNodeViewModifier, VersionTag}
import scorex.crypto.authds.ADDigest

import scala.util.Try

trait EncryBaseState[P <: Proposition, BB <: BaseBoxBody, IState <: MinimalState[EncryPersistentModifier, IState]]
  extends MinimalState[EncryPersistentModifier, IState] with ScorexLogging {

  self: IState =>

  def rootHash(): ADDigest

  // TODO: Implement correctly.
  def stateHeight(): Int = 0

  // TODO: Which instance of proposition should be passed here??
  def boxChanges(txs: Seq[EphemerealNodeViewModifier]): EncryBoxStateChanges

  // ID of last applied modifier.
  override def version: VersionTag

  override def applyModifier(mod: EncryPersistentModifier): Try[IState]

  override def rollbackTo(version: VersionTag): Try[IState]

  def rollbackVersions: Iterable[VersionTag]

  override type NVCT = this.type

}

object EncryBaseState extends ScorexLogging
