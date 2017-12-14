package encry.modifiers.history

import encry.modifiers.EncryPersistentModifier
import encry.modifiers.history.EncryBlockHeader
import encry.modifiers.history.EncryBlockPayload
import encry.modifiers.mempool.{EncryBaseTransaction, EncryPaymentTransaction}
import encry.settings.Algos
import io.circe.Json
import io.circe.syntax._
import scorex.core.{EphemerealNodeViewModifier, ModifierId, ModifierTypeId}
import scorex.core.transaction.box.proposition.PublicKey25519Proposition
import scorex.core.serialization.Serializer

import scala.util.Try

class EncryFullBlock(override val header: EncryBlockHeader,
                 override val payload: EncryBlockPayload)
  extends EncryBaseBlock{

  override type M = EncryFullBlock

  override val toSeq: Seq[EncryPersistentModifier] = Seq(header, payload)

  def transactions: Seq[EphemerealNodeViewModifier] = payload.transactions

  override def parentId: ModifierId = header.parentId

  override val modifierTypeId: ModifierTypeId = EncryBlock.modifierTypeId

  override lazy val id: ModifierId = ModifierId @@ Algos.hash(header.id ++ payload.id)

  override def serializer: Serializer[EncryFullBlock] = EncryPaymentBlockSerializer

  override lazy val json: Json = Map(
    "header" -> header.json,
    "payload" -> payload.json,
    //    "adPoofs" -> aDProofs.map(_.json).getOrElse(Map.empty[String, String].asJson)
  ).asJson
}

object EncryBlock {
  val modifierTypeId: ModifierTypeId = ModifierTypeId @@ (-127: Byte)
}

object EncryPaymentBlockSerializer extends Serializer[EncryFullBlock] {

  override def toBytes(obj: EncryFullBlock): Array[Byte] = ???

  override def parseBytes(bytes: Array[Byte]): Try[EncryFullBlock] = ???
}
