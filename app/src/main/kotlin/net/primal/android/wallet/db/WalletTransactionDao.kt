package net.primal.android.wallet.db

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface WalletTransactionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsertAll(data: List<WalletTransactionData>)

    @Transaction
    @Query("SELECT * FROM WalletTransactionData ORDER BY completedAt DESC")
    fun latestTransactionsPaged(): PagingSource<Int, WalletTransaction>

    @Query("SELECT * FROM WalletTransactionData ORDER BY createdAt DESC LIMIT 1")
    fun first(): WalletTransactionData?

    @Query("SELECT * FROM WalletTransactionData ORDER BY createdAt ASC LIMIT 1")
    fun last(): WalletTransactionData?
}
