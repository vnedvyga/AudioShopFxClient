package ua.org.oa.nedvygav.models;


import java.util.ArrayList;
import java.util.List;

public class Cart {
    private static Cart CART_INSTANCE;

    private List<Audio> audios = new ArrayList<>();

    private Cart() {

    }

    public static Cart getCart(){
        if (CART_INSTANCE==null){
            CART_INSTANCE=new Cart();
        }
        return CART_INSTANCE;
    }

    public List<Audio> getItems(){
        return audios;
    }

    public boolean addItem(Audio item){
        return audios.add(item);
    }

    public boolean addAllItems(List<Audio> list){
        return audios.addAll(list);
    }

    public boolean clearCart(){
        audios.clear();
        if (audios.size()==0){
            return true;
        } else  return false;
    }

    public boolean removeItem(Audio item){
        return audios.remove(item);
    }
}
