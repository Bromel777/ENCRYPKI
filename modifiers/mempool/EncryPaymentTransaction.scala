package encry.modifiers.mempool

import encry.modifiers.mempool.EncryBaseTransaction._
import encry.modifiers.mempool.box.body.{BaseBoxBody, PaymentBoxBody}
import encry.modifiers.mempool.box._
import encry.settings.Algos
import com.google.common.primitives.{Bytes, Ints, Longs}
import com.sun.jndi.cosnaming.IiopUrl.Address
import encry.modifiers.mempool.box.unlockers.EncryPaymentBoxUnlocker
import io.circe.Json
import io.circe.syntax._
import scorex.core.serialization.Serializer
import scorex.core.transaction.box.proposition.PublicKey25519Proposition
import scorex.core.transaction.box.Box.Amount
import scorex.core.transaction.box.BoxUnlocker
import scorex.core.transaction.proof.Signature25519
import scorex.crypto.authds.ADKey
import scorex.crypto.encode.Base58
import scorex.crypto.hash.Digest32

import scala.util.Try

case class EncryPaymentTransaction(val senderProp : PublicKey25519Proposition,
                                   override val fee: Amount,
                                   override val timestamp: Long,
                                   override val unlockers: IndexedSeq[ADKey],
                                   signature: Signature25519,
                                   createOutputs: IndexedSeq[(PublicKey25519Proposition, Amount)])
  extends EncryBaseTransaction[PublicKey25519Proposition,PaymentBoxBody] {



  override type M = EncryPaymentTransaction

  // Type of actual Tx type.
  override val typeId: TxTypeId = 1.toByte

  val newBoxes: Traversable[EncryBaseBox[PublicKey25519Proposition,BaseBoxBody]] = createOutputs.zipWithIndex.map { case ((pkp, amount), idx) =>
    val nonce = nonceFromDigest(Algos.hash(hashNoNonces ++ Ints.toByteArray(idx)))
    EncryPaymentBox(AddressProposition(pkp.address), nonce, PaymentBoxBody(amount)).asInstanceOf[EncryBaseBox[PublicKey25519Proposition,BaseBoxBody]]
  }

  override def serializer: Serializer[EncryPaymentTransaction] = EncryPaymentTransactionSerializer

  override def json: Json = Map(
    "id" -> Base58.encode(id).asJson,
    "inputs" -> unlockers.map { id =>
      Map(
        "NoncedBox ID" -> Algos.encode(id).asJson,
      ).asJson
    }.asJson,
    "outputs" -> createOutputs.map { case (_, amount) =>
      Map(
        "script" -> "".asJson,
        "amount" -> amount.asJson
      ).asJson
    }.asJson
  ).asJson

  lazy val hashNoNonces: Digest32 = Algos.hash(
    Bytes.concat(scorex.core.utils.concatFixLengthBytes(unlockers),
      scorex.core.utils.concatFixLengthBytes(createOutputs.map { case (pkp, amount) =>
        pkp.pubKeyBytes ++ Longs.toByteArray(amount)
      })
    )
  )

  lazy val unlockersHash: Digest32 = Algos.hash(
    scorex.core.utils.concatFixLengthBytes(
      unlockers.map{
        case(boxId) => {
          boxId
        }
      }
    )
  )

  lazy val noncedboxHash: Digest32 = Algos.hash(
    scorex.core.utils.concatFixLengthBytes(
      createOutputs.map{
        case(prop,amount) => {
          prop.pubKeyBytes ++ Longs.toByteArray(amount)
        }
      }
    )
  )

  lazy val hashInfo : Digest32 = Algos.hash(
    senderProp.pubKeyBytes ++ Longs.toByteArray(fee) ++ Longs.toByteArray(timestamp)
  )

  //В дальнейшем используется как TXkey для поиска в mempool

  lazy val trxHash: Digest32 = Algos.hash(
    unlockersHash ++ noncedboxHash ++ hashInfo
  )

  override val messageToSign: Array[Byte] = trxHash

}

object EncryPaymentTransactionSerializer extends Serializer[EncryPaymentTransaction] {

  override def toBytes(obj: EncryPaymentTransaction): Array[Byte] = ???

  override def parseBytes(bytes: Array[Byte]): Try[EncryPaymentTransaction] = ???
}
