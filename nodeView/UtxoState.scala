package encry.nodeView

import akka.actor.ActorRef
import encry.modifiers.EncryPersistentModifier
import encry.modifiers.history.EncryFullBlock
import encry.modifiers.mempool.box.body.BaseBoxBody
import encry.modifiers.mempool.box._
import encry.modifiers.mempool.{EncryBaseTransaction, EncryPaymentTransaction}
import encry.settings.Algos
import io.iohk.iodb.{ByteArrayWrapper, Store}
import scorex.core.transaction.box.proposition.{Proposition, PublicKey25519Proposition}
import scorex.core.transaction.state.TransactionValidation
import scorex.core.utils.ScorexLogging
import scorex.core.{EphemerealNodeViewModifier, VersionTag}
import scorex.crypto.authds.avltree.batch.{BatchAVLProver, NodeParameters, PersistentBatchAVLProver, VersionedIODBAVLStorage}
import scorex.crypto.authds.{ADDigest, ADKey}
import scorex.crypto.hash.{Blake2b256Unsafe, Digest32}

import scala.util.Try

class UtxoState(override val version: VersionTag,
                val store: Store,
                nodeViewHolderRef: Option[ActorRef])
  extends EncryBaseState[Proposition, BaseBoxBody, UtxoState]
    with TransactionValidation[PublicKey25519Proposition, EncryPaymentTransaction] with ScorexLogging {


  implicit val hf = new Blake2b256Unsafe
  private lazy val np = NodeParameters(keySize = 32, valueSize = EncryPaymentBoxSerializer.Length, labelSize = 32)
  protected lazy val storage = new VersionedIODBAVLStorage(store, np)

  protected lazy val persistentProver: PersistentBatchAVLProver[Digest32, Blake2b256Unsafe] =
    PersistentBatchAVLProver.create(
      new BatchAVLProver[Digest32, Blake2b256Unsafe](keyLength = 32,
        valueLengthOpt = Some(EncryPaymentBoxSerializer.Length)),
      storage,
    ).get


  // TODO: Why 10?
  override def maxRollbackDepth: Int = 10

  def boxChanges(txs: Seq[EphemerealNodeViewModifier]): EncryBoxStateChanges = {
    EncryBoxStateChanges(
      txs.flatMap(tx => tx match {
        case trx : EncryPaymentTransaction =>{
          trx.unlockers.filter(key => {
            //TODO: init db
//            val nb = store.get(new ByteArrayWrapper(key))
//
//            require(nb != null,"Empty data with this ADKey")
//            nb match {
//              case b : EncryPaymentBox =>{
//                require(b.proposition.addrBytes == trx.senderProp.address,"Incorrect unlocker. Invalid Address")
//              }
//              case _ =>{
//                require(false,"Incorrect box from storage")
//              }
//            }
            true
          }).map(
            key => Removal(key)
          )
        }
      })
    )
  }

//      txs.flatMap( tx =>
//      tx match {
//        case tx: EncryPaymentTransaction => {
//          tx.unlockers.filter {
//            unl => true //unl.boxKey.isValid(proposition, tx.messageToSign)
//          }.map(unl => Removal(ADKey @@ unl.closedBoxId).asInstanceOf[EncryBoxStateChangeOperation]) ++
//            //tx.newBoxes.map(bx => Insertion(bx).asInstanceOf[EncryBoxStateChangeOperation])
//        }
////        case tx: _ => {
////          require(false,"Unsupported trx type")
////        }
//      }
//    )


  // Dispatches applying `Modifier` of particular type.
  override def applyModifier(mod: EncryPersistentModifier): Try[UtxoState] = mod match {
    case pb: EncryFullBlock => {
      //TODO: Impliment
//      log.debug(s"Applying block with header ${pb.header.encodedId} to UtxoState with " +
//        s"root hash ${Algos.encode(rootHash)}")
      Try{this}
    }

  }

  override def rollbackTo(version: VersionTag): Try[UtxoState] = ???

  override def rollbackVersions: Iterable[VersionTag] = ???

  override lazy val rootHash: ADDigest = ???

  override def validate(tx: EncryPaymentTransaction): Try[Unit] = ???

}

object UtxoState {

  private lazy val bestVersionKey = Algos.hash("best state version") // TODO: ???

  private def metadata(modId: VersionTag, stateRoot: ADDigest): Seq[(Array[Byte], Array[Byte])] = {
    val idStateDigestIdxElem: (Array[Byte], Array[Byte]) = modId -> stateRoot
    val stateDigestIdIdxElem = Algos.hash(stateRoot) -> modId
    val bestVersion = bestVersionKey -> modId

    Seq(idStateDigestIdxElem, stateDigestIdIdxElem, bestVersion)
  }
}
