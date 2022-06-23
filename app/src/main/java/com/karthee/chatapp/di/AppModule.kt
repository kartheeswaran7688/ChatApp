package com.karthee.chatapp.di

import android.content.Context
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.karthee.chatapp.core.MessageStatusUpdater
import com.karthee.chatapp.db.DbRepository
import com.karthee.chatapp.db.DefaultDbRepo
import com.karthee.chatapp.db.daos.ChatUserDao
import com.karthee.chatapp.db.daos.GroupDao
import com.karthee.chatapp.db.daos.GroupMessageDao
import com.karthee.chatapp.db.daos.MessageDao
import com.karthee.chatapp.ui.activities.MainActivity
import com.karthee.chatapp.utils.MPreference
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.internal.managers.ApplicationComponentManager
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class MessageCollection

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class GroupCollection

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideFireStoreInstance(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }

    @Singleton
    @Provides
    fun provideUsersCollectionRef(firestore: FirebaseFirestore): CollectionReference {
        return firestore.collection("Users")
    }

    @MessageCollection
    @Singleton
    @Provides
    fun provideMessagesCollectionRef(firestore: FirebaseFirestore): CollectionReference {
        return firestore.collection("Messages")
    }

    @GroupCollection
    @Singleton
    @Provides
    fun provideGroupCollectionRef(firestore: FirebaseFirestore): CollectionReference {
        return firestore.collection("Groups")
    }

    @Provides
    fun provideMainActivity(): MainActivity {
        return MainActivity()
    }

    @Provides
    fun provideDefaultDbRepo(userDao: ChatUserDao,
                             groupDao: GroupDao,
                             groupMsgDao: GroupMessageDao,
                             messageDao: MessageDao): DefaultDbRepo {
        return DbRepository(userDao, groupDao, groupMsgDao, messageDao)
    }

}