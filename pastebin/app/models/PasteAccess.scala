package models

import java.util.UUID
import java.time.LocalDateTime
import java.sql.Date

abstract class Visibility(val name: String){
    override def toString = name
}
object Visibility{
    final case class Public() extends Visibility("public")
    final case class Private() extends Visibility("private")
}

abstract class Life(val name: String, expires: Option[LocalDateTime]){
    override def toString = name
    val days: Int = 0
}
object Life{
    final case class Forever() extends Life("forever", None){
        override def toString = s"lifetime"
        override val days = -1
    }

    final case class Expires(override val days: Int) extends Life("expires", Some((LocalDateTime.now).plusDays(days))){
        override def toString = s"$days day(s)"
    }

    case class Expired() extends Life("expired", None){
        lazy val expired_when: Option[LocalDateTime] = None
        override def toString = "no"
    }
}

trait AccessControl{
    val visibility: Visibility
    val lifetime: Life
}


trait AccessKeys{
    val deletion_key: UUID
    val creation_date: Date = Date.valueOf(LocalDateTime.now.toLocalDate)
}

case class PasteAccess(val paste_id: UUID) extends AccessControl with AccessKeys{
    lazy val deletion_key = UUID.randomUUID

    lazy val visibility: Visibility = new Visibility.Public()
    lazy val lifetime: Life = Life.Expires(30)

    def toRow = PasteAccess.toRow(this)
}

case class PasteAccessRow(val paste_id: UUID, visibility: String, lifetime: Int, deletion_key: UUID, creation_date: Date){
    def toPasteAccess = PasteAccess.fromRow(this)
}

object PasteAccess{
    def expires(days: Int)(paste_id: UUID) = new PasteAccess(paste_id){
        override lazy val lifetime = new Life.Expires(days)
    }

    def forever(paste_id: UUID) = new PasteAccess(paste_id){
        override lazy val lifetime = new Life.Forever()
    }

    def `private`(paste_id: UUID)(life: Life = Life.Expires(30)) = new PasteAccess(paste_id){
        override lazy val visibility = new Visibility.Private
        override lazy val lifetime = life
    }

    def `public`(paste_id: UUID)(implicit life: Life = Life.Expires(30)) = new PasteAccess(paste_id){
        override lazy val visibility = new Visibility.Public
        override lazy val lifetime = life
    }

    def expired(paste_id: UUID)(when: LocalDateTime) = new PasteAccess(paste_id){
        override lazy val lifetime = new Life.Expired(){
            override lazy val expired_when = Some(when)
        }
    }

    def fromRow(row: PasteAccessRow) = new PasteAccess(row.paste_id){
        override lazy val lifetime = row.lifetime match{
            case -1 => Life.Forever()
            case 0 => Life.Expired()
            case _ => Life.Expires(row.lifetime)
        }

        override lazy val visibility = row.visibility match{
            case "public" => Visibility.Public()
            case "private" => Visibility.Private()
        }

        override lazy val deletion_key = row.deletion_key  
        override val creation_date = row.creation_date     
    }

    def toRow(pa: PasteAccess) = new PasteAccessRow(
        pa.paste_id,
        pa.visibility match{
            case Visibility.Public() => "public"
            case Visibility.Private() => "private"
        },
        pa.lifetime match{
            case Life.Expired() => 0
            case Life.Expires(i) => i
            case Life.Forever() => -1
        },
        pa.deletion_key,
        pa.creation_date
    )
    
}