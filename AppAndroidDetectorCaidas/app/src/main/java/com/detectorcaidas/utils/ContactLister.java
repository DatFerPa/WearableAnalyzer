package com.detectorcaidas.utils;


import android.content.ContentResolver;
import android.database.Cursor;
import android.provider.ContactsContract;
import com.detectorcaidas.recycle.Contacto;
import java.util.ArrayList;
import java.util.List;

public class ContactLister {

    private static final String TAG = "ContactLister";
    private List<Contacto> contactos;

    public ContactLister() {
        this.contactos = new ArrayList<>();
    }

    public List<Contacto> getContactos() {
        return contactos;
    }

    public  void getListaDeContactos(ContentResolver cr) {

        Cursor phones = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,null,null, null);
        String oldphoneNumber = "-1";
        while (phones.moveToNext()){
            String name=phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            if(!phoneNumber.equals(oldphoneNumber)) {
                contactos.add(new Contacto(name, phoneNumber));
            }
            oldphoneNumber = phoneNumber;
        }
    }


}