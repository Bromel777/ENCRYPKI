package encry.modifiers.history

import encry.modifiers.EncryPersistentModifier
import encry.modifiers.history.EncryBlockHeader
import encry.modifiers.history.EncryBaseBlockPayload
import scorex.core.{NodeViewModifier, PersistentNodeViewModifier, TransactionsCarryingPersistentNodeViewModifier}
import scorex.core.transaction.Transaction
import scorex.core.transaction.box.proposition.Proposition

trait EncryBaseBlock
  extends EncryPersistentModifier with PersistentNodeViewModifier{

  val header: EncryBlockHeader

  val payload : EncryBlockPayload

  val toSeq: Seq[EncryPersistentModifier]

}

