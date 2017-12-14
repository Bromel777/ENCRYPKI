package encry.modifiers.history

import scorex.core.{EphemerealNodeViewModifier, ModifierId, TransactionsCarryingPersistentNodeViewModifier}
import encry.modifiers.{EncryPersistentModifier, ModifierWithDigest}
import scorex.core.transaction.Transaction
import scorex.core.transaction.box.proposition.Proposition

abstract class EncryBaseBlockPayload
  extends EncryPersistentModifier with ModifierWithDigest {

  val headerId: ModifierId
  val txs: Seq[EphemerealNodeViewModifier]
}