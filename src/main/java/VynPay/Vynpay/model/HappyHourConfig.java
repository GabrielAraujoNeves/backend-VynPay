package VynPay.Vynpay.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "happy_hour_config")
public class HappyHourConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Boolean isActive = false;

    @Column(nullable = false)
    private Double discountPercent;

    @Column(name = "start_time")
    private LocalTime startTime;

    @Column(name = "end_time")
    private LocalTime endTime;

    // 🔥 NOVO: Dias da semana (ex: "MON,TUE,WED,THU,FRI")
    @Column(name = "days_of_week", length = 50)
    private String daysOfWeek;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "happyHourConfig", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<HappyHourProduct> happyHourProducts = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // 🔥 Métodos auxiliares para dias da semana
    public List<String> getDaysOfWeekList() {
        if (daysOfWeek == null || daysOfWeek.isEmpty()) {
            return null;
        }
        return Arrays.asList(daysOfWeek.split(","));
    }

    public void setDaysOfWeekList(List<String> days) {
        if (days == null || days.isEmpty()) {
            this.daysOfWeek = null;
        } else {
            this.daysOfWeek = String.join(",", days);
        }
    }
}