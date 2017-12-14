package encry.modifiers.history

import encry.modifiers.EncryPersistentModifier
import encry.modifiers.history.EncryBlockHeader
import encry.modifiers.history.EncryBaseBlockPayload
import scorex.core.TransactionsCarryingPersistentNodeViewModifier
import scorex.core.transaction.Transaction
import scorex.core.transaction.box.proposition.Proposition

trait EncryBaseBlock[P <: Proposition, TX <: Transaction[P], BP <: EncryBaseBlockPayload[P, TX]]
  extends EncryPersistentModifier with TransactionsCarryingPersistentNodeViewModifier[P, TX] {

  val header: EncryBlockHeader

  val payload: BP

  val toSeq: Seq[EncryPersistentModifier]

}

