package actors

import java.util.UUID
import models._


case class PasteCreated(paste: Paste, access: PasteAccess)
case class PasteDestroyed(paste: Paste, access: PasteAccess)
case class PasteAccessed(paste: Paste, access: PasteAccess)

case class IndexItem(paste: Paste)
case class ItemIndexed(paste: Paste)
case class RemoveIndexedItem(paste: Paste)
case class IndexedIntemRemoved(paste: Paste)

case class PasteSearch(query: String)