//package encry.nodeView.state
//
//import encry.modifiers.EncryPersistentModifier
//import encry.modifiers.mempool.EncryBaseTransaction
//import encry.modifiers.mempool.box.EncryBaseBox
//import encry.modifiers.mempool.box.body.BaseBoxBody
//import scorex.core.VersionTag
//import scorex.core.transaction.box.Box
//import scorex.core.transaction.box.proposition.Proposition
//import scorex.core.transaction.state.{BoxStateChanges, Insertion, MinimalState, Removal}
//import scorex.core.utils.ScorexLogging
//import scorex.crypto.authds.{ADDigest, ADKey}
//
//import scala.util.Try
//
//trait EncryBaseState[P <: Proposition, BB <: BaseBoxBody, BX <: EncryBaseBox[P, BB],
//  TX <: EncryBaseTransaction[P, BB, BX], IState <: MinimalState[EncryPersistentModifier, IState]]
//  extends MinimalState[EncryPersistentModifier, IState] with ScorexLogging {
//
//  self: IState =>
//
//  def rootHash(): ADDigest
//
//  // TODO: Implement correctly.
//  def stateHeight(): Int = 0
//
//  // TODO: Which instance of proposition should be passed here??
//  def boxChanges(txs: Seq[TX], proposition: P): BoxStateChanges[P, BX] =
//    BoxStateChanges[P, BX](txs.flatMap { tx =>
//    tx.unlockers.filter { unl =>
//      unl.boxKey.isValid(proposition, tx.messageToSign) }
//      .map( unl => Removal[P, BX](ADKey @@ unl.closedBoxId)) ++
//      tx.newBoxes.map(bx => Insertion[P, BX](bx))
//  })
//
//  // ID of last applied modifier.
//  override def version: VersionTag
//
//  override def applyModifier(mod: EncryPersistentModifier): Try[IState]
//
//  override def rollbackTo(version: VersionTag): Try[IState]
//
//  def rollbackVersions: Iterable[VersionTag]
//
//  override type NVCT = this.type
//
//}
//
//object EncryBaseState extends ScorexLogging
