import models.subscription.AccountingPeriod
import java.time.LocalDate

val accountingPeriod = AccountingPeriod(
  startDate = LocalDate.parse("2024-01-01"),
  endDate = LocalDate.parse("2024-12-31"),
  dueDate = Some(LocalDate.parse("2025-01-01"))
)

println(accountingPeriod)
println(accountingPeriod.toString)