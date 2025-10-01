# DOLL

커스텀 Npc 프레임워크

---

* ### Features
  * FakeEntity/Reality 객체를 활용한 이중 엔티티 구현
  * 시그널과 체이닝을 이용한 _Movement_ 제어
  * _Chimera_ 를 활용한 병렬 인형 구현
  * 커스텀 모델
  * _Clicked_ 키워드를 통한 액션 제어
  * _Ticker_ 을 통한 틱 단위 스케줄링 관리 **[각별-Tap](https://github.com/monun/tap)**
* ### Supported Minecraft Versions
  * 1.21

`설정`
```Kotlin
class YourPlugin : JavaPlugin() {
    override fun onEnable() {
        initDoLL()
    }

    override fun onDisable() {
        shutDownDoLL()
    }
}
```


* ### Implementation
  * ProtocoLib 5.3.0
  * Kotlin Runtime