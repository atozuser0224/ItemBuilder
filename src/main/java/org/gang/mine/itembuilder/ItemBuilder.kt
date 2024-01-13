package org.gang.mine.itembuilder

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType
import java.util.*

open class ItemBuilder{
    private var item: ItemStack
    private var itemMeta: ItemMeta
    private var persistence:PersistentDataContainer
    var material:Material
    var amount: Int
    var lore = mutableListOf<String>()

    constructor(
        name:String,  material: Material = Material.AIR,  amount: Int = 1
    ){
        item = ItemStack(material,amount)
        this.material = material
        this.amount = amount
        itemMeta = item.itemMeta
        persistence = itemMeta.persistentDataContainer
        setName(item.itemMeta.displayName)
    }
    constructor(item:ItemStack){
        this.item = item
        this.amount = this.item.amount
        material = this.item.type
        itemMeta = this.item.itemMeta
        persistence = itemMeta.persistentDataContainer
        setName(this.item.itemMeta.displayName)
    }

    fun getItem(): ItemStack {
        itemMeta.lore = lore
        item.setItemMeta(itemMeta)
        item.amount = amount
        return item
    }
    fun setName(name: String): ItemBuilder {
        itemMeta.setDisplayName(name)
        return this
    }
    fun setMaterial(material: Material): ItemBuilder {
        this.material = material
        return this
    }

    fun setAmount(amount: Int): ItemBuilder {
        this.amount = amount
        return this
    }

    fun addAttribute(attributeKey: org.bukkit.attribute.Attribute,
                     key : String,
                     value : Double,
                     slot: EquipmentSlot,
                     operation:AttributeModifier.Operation=AttributeModifier.Operation.ADD_NUMBER): ItemBuilder {
        val attribute = AttributeModifier(UUID.randomUUID(),key,value,operation,slot)
        itemMeta.addAttributeModifier(attributeKey,attribute)
        return this;
    }
    fun removeAttribute(attribute: Attribute): ItemBuilder {
        itemMeta.removeAttributeModifier(attribute)
        return this
    }

    fun <T> setData(key: String, type: PersistentDataType<T, T>, value: T&Any): ItemBuilder {
        val namespacedKey = NamespacedKey.fromString(key)
        return if (namespacedKey != null) {
            persistence.set(namespacedKey, type, value)
            this
        } else {
            this
        }
    }
    fun <T> getDataOrDefault(key: String, type: PersistentDataType<T, T>,default: T & Any): Data<T> {
        return Data<T>(this,if (NamespacedKey.fromString(key) == null){
            null
        }else{
            this.itemMeta.persistentDataContainer.getOrDefault(NamespacedKey.fromString(key)!!,type,default)
        })
    }
    fun setLore(lore:MutableList<String>): ItemBuilder {
        this.lore = lore
        return this
    }
    fun addLore(line : String): ItemBuilder {
        lore.add(line)
        return this
    }
    fun resetLore(): ItemBuilder {
        lore.clear()
        return this
    }
    fun removeLore(index:Int){
        try {
            lore.removeAt(index)
        }catch (e:IndexOutOfBoundsException){
            Bukkit.getLogger().warning("item index error")
        }
    }
    fun addEnchant(enchantment: Enchantment,value: Int): ItemBuilder {
        itemMeta.addEnchant(enchantment, value,true)
        return this
    }
    fun removeEnchant(enchantment: Enchantment): ItemBuilder {
        itemMeta.removeEnchant(enchantment)
        return this
    }
    class Data<T>(private val itemBuilder: ItemBuilder, val value: Any?){

        fun addEnchant(enchantment: Enchantment): ItemBuilder {
            require(this.value is Int) { "Value must be of type Int" }
            itemBuilder.addEnchant(enchantment, value)
            return itemBuilder
        }
        fun addAttribute(attributeKey: Attribute,
                         key : String,
                         slot: EquipmentSlot,
                         operation:AttributeModifier.Operation=AttributeModifier.Operation.ADD_NUMBER): ItemBuilder {
            require(value is Double)
            val attribute = AttributeModifier(UUID.randomUUID(),key,value,operation,slot)
            itemBuilder.itemMeta.addAttributeModifier(attributeKey,attribute)
            return itemBuilder;
        }
        fun <T> to(): Data<T> {
            return Data<T>(this.itemBuilder,this.value)
        }
    }
}

fun <T> ItemStack.getDataOrDefault(key: String, type: PersistentDataType<T, T>,default: T & Any):T?{
    return if (NamespacedKey.fromString(key) == null){
        null
    }else{
        this.itemMeta.persistentDataContainer.getOrDefault(NamespacedKey.fromString(key)!!,type,default)
    }
}
fun ItemStack.builder()= ItemBuilder(this)