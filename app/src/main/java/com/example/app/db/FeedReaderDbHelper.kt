package com.example.app.db

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import com.example.app.R.drawable.sample_product_image
import com.example.app.db.FeedReaderContract.SQL_CREATE_ENTRIES
import com.example.app.db.FeedReaderContract.SQL_DELETE_ENTRIES

class FeedReaderDbHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(SQL_CREATE_ENTRIES)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // On supprime les anciennes données lors de la mise à jour de la base
        db.execSQL(SQL_DELETE_ENTRIES)
        onCreate(db)
    }

    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        onUpgrade(db, oldVersion, newVersion)
    }

    // Méthode pour insérer un produit
    fun insertProduct(db: SQLiteDatabase, product: Product): Long {
        val values = ContentValues().apply {
            put(FeedReaderContract.FeedEntry.COLUMN_NAME_TITLE, product.name)
            put(FeedReaderContract.FeedEntry.COLUMN_NAME_SUBTITLE, product.description)
        }
        // Insérer dans la table "entry"
        return db.insert(FeedReaderContract.FeedEntry.TABLE_NAME, null, values)
    }

    // Méthode pour lire un produit
    @Composable
    fun readProduct(db: SQLiteDatabase, productId: Long): Product? {
        val cursor = db.query(
            FeedReaderContract.FeedEntry.TABLE_NAME,
            null, // Tous les colonnes
            "${BaseColumns._ID} = ?",
            arrayOf(productId.toString()),
            null,
            null,
            null
        )

        return if (cursor.moveToFirst()) {
            val name = cursor.getString(cursor.getColumnIndexOrThrow(FeedReaderContract.FeedEntry.COLUMN_NAME_TITLE))
            val description = cursor.getString(cursor.getColumnIndexOrThrow(FeedReaderContract.FeedEntry.COLUMN_NAME_SUBTITLE))
            cursor.close()
            Product(name, description, painterResource(id = sample_product_image)) // Remplacez avec votre image dynamique
        } else {
            cursor.close()
            null
        }
    }

    // Méthode pour mettre à jour un produit
    fun updateProduct(db: SQLiteDatabase, productId: Long, updatedProduct: Product): Int {
        val values = ContentValues().apply {
            put(FeedReaderContract.FeedEntry.COLUMN_NAME_TITLE, updatedProduct.name)
            put(FeedReaderContract.FeedEntry.COLUMN_NAME_SUBTITLE, updatedProduct.description)
        }
        // Mettre à jour le produit où _ID = productId
        return db.update(
            FeedReaderContract.FeedEntry.TABLE_NAME,
            values,
            "${BaseColumns._ID} = ?",
            arrayOf(productId.toString())
        )
    }

    // Méthode pour supprimer un produit
    fun deleteProduct(db: SQLiteDatabase, productId: Long): Int {
        // Supprimer un produit où _ID = productId
        return db.delete(
            FeedReaderContract.FeedEntry.TABLE_NAME,
            "${BaseColumns._ID} = ?",
            arrayOf(productId.toString())
        )
    }

    companion object {
        const val DATABASE_VERSION = 1
        const val DATABASE_NAME = "FeedReader.db"
    }
}
