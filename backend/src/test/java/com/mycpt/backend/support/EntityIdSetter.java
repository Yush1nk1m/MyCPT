package com.mycpt.backend.support;

public class EntityIdSetter {

    /**
     * 엔티티의 id 필드에 값을 주입한다.
     * 상속 계층을 거슬러 올라가며 id 필드를 탐색하므로
     * CTI 자식 엔티티(DiscTest 등)에도 사용 가능하다.
     */
    public static <T> T setId(T target, Long id) {
        Class<?> clazz = target.getClass();
        while (clazz != null) {
            try {
                var field = clazz.getDeclaredField("id");
                field.setAccessible(true);
                field.set(target, id);
                return target;
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new RuntimeException("id 필드를 찾을 수 없음: " + target.getClass().getName());
    }
}
