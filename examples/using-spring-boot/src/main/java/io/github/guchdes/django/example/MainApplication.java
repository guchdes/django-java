package io.github.guchdes.django.example;

import com.mongodb.ClientSessionOptions;
import com.mongodb.TransactionOptions;
import io.github.guchdes.django.bson.projection.DocumentMap;
import io.github.guchdes.django.core.DatabaseDao;
import io.github.guchdes.django.core.spring.BoolValue;
import io.github.guchdes.django.core.spring.DjangoInject;
import io.github.guchdes.django.core.spring.EnableDjango;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableDjango
public class MainApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(MainApplication.class, args);
    }

    @DjangoInject(enableCache = BoolValue.FALSE)
    DatabaseDao dao;

    @Override
    public void run(String... args) throws Exception {
        //创建equip对象
        Equip equip = Equip.create();
        equip.setName("麻痹戒指");
        equip.setEquipId(1);
        equip.setLevel(1);

        Equip equip2 = Equip.create();
        equip2.setName("屠龙宝刀");
        equip2.setEquipId(2);
        equip2.setLevel(1);

        //创建bag对象，并把equip对象put到bag
        Bag bag = Bag.create();
        bag.setId("player1");
        bag.setEquips(new DocumentMap<>());
        bag.getEquips().put(equip.getEquipId(), equip);
        bag.getEquips().put(equip2.getEquipId(), equip2);

        //保存bag文档，此时执行的是 insert
        dao.saveByKey(bag);

        //更新equip的level
        equip.setLevel(2);

        //再次保存bag文档，此时执行的是 update({_id:"001", {$set: {'equips.1.level': 2}}})
        dao.saveByKey(bag);

        //查找bag文档, Bag类没有定义key，默认id就是key，所以是根据id查找
        Bag find = dao.findByKey(Bag.class, "player1");
        assert find != null;
        assert find.getEquips().get(1).getLevel() == 2;

        //删除bag文档
        boolean delete = dao.deleteByKey(bag);
        assert delete;
    }

    private void runTx() {
        dao.withNewSessionTransaction(sessionDao -> {
            Bag bag1 = Bag.create();
            Bag bag2 = Bag.create();
            bag1.setId("1");
            bag2.setId("2");
            sessionDao.saveByKey(bag1);
            sessionDao.saveByKey(bag2);
            return true;
        }, null, null);
    }

}
