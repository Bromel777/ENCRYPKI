package encry.modifiers.history

import com.google.common.primitives.{Bytes, Longs, Shorts}
import encry.modifiers.{EncryPersistentModifier, ModifierWithDigest}
import encry.modifiers.mempool.{EncryPaymentTransaction, EncryPaymentTransactionSerializer}
import encry.settings.{Algos, Constants}
import io.circe.Json
import io.circe.syntax._
import scorex.core.serialization.Serializer
import scorex.core.transaction.box.proposition.PublicKey25519Proposition
import scorex.core.{ModifierId, ModifierTypeId, TransactionsCarryingPersistentNodeViewModifier}
import scorex.crypto.authds.LeafData
import scorex.crypto.encode.Base58
import scorex.crypto.hash.Digest32
import encry.utils.concatBytes

import scala.util.Try

case class EncryTransactionBlock(override val transactions : Seq[EncryPaymentTransaction]) extends EncryPersistentModifier
  with TransactionsCarryingPersistentNodeViewModifier[PublicKey25519Proposition, EncryPaymentTransaction]
                                                                          with ModifierWithDigest{

  override val modifierTypeId = EncryTransactionBlock.modifierTypeId

  override val id : ModifierId = ModifierId @@ Longs.toByteArray(999L)

  override def json = List(12).asJson

  override type M = EncryTransactionBlock

  def digest: Digest32 = EncryTransactionBlock.rootHash(LeafData @@ transactions.map(_.id))

  override def serializer = EncryTransactionBlockSerializer
}

object EncryTransactionBlock {
  val modifierTypeId: ModifierTypeId = ModifierTypeId @@ (102: Byte)

  def rootHash(ids: Seq[LeafData]): Digest32 = Algos.merkleTreeRoot(ids)
}

object EncryTransactionBlockSerializer extends Serializer[EncryTransactionBlock] {
  override def toBytes(obj: EncryTransactionBlock): Array[Byte] = {
    val txsBytes = concatBytes(obj.transactions.map{tx =>
      assert(tx.bytes.length.toShort % 8 == 0)
      Bytes.concat(Shorts.toByteArray(tx.bytes.length.toShort), tx.bytes)})
    Bytes.concat(txsBytes)
  }

  override def parseBytes(bytes: Array[Byte]): Try[EncryTransactionBlock] = Try {
    val headerId: ModifierId = ModifierId @@ bytes.slice(0, Constants.ModifierIdSize)

    def parseTransactions(index: Int, acc: Seq[EncryPaymentTransaction]): EncryTransactionBlock = {
      if (index == bytes.length) {
        EncryTransactionBlock(acc)
      } else {
        val txLength = Shorts.fromByteArray(bytes.slice(index, index + 2)).ensuring(_ % 8 == 0)
        val tx = EncryPaymentTransactionSerializer.parseBytes(bytes.slice(index + 2, index + 2 + txLength)).get
        parseTransactions(index + 2 + txLength, acc :+ tx)
      }
    }
    parseTransactions(Constants.ModifierIdSize, Seq())
  }
}