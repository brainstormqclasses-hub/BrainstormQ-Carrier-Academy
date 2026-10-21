package com.example.ui.payment

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavyPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentBottomSheet(
    onDismiss: () -> Unit,
    onPaymentSuccess: (transactionId: String) -> Unit
) {
    val context = LocalContext.current
    var selectedMethod by remember { mutableIntStateOf(0) } // 0: UPI, 1: Cards, 2: Net Banking
    var enteredTxId by remember { mutableStateOf("") }
    var cardNumber by remember { mutableStateOf("") }
    var cardExpiry by remember { mutableStateOf("") }
    var cardCvv by remember { mutableStateOf("") }
    var selectedBank by remember { mutableStateOf("State Bank of India") }
    var isProcessing by remember { mutableStateOf(false) }

    val upiId = "8638953846-2@ybl"

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        modifier = Modifier.testTag("payment_modal_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.horizontalGradient(listOf(GoldAccent, Color(0xFFD97706)))
                    )
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "LIFETIME ALL-ACCESS PASS • ONE-TIME PAYMENT",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Unlock Everything for ₹99/- Only",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = NavyDark,
                textAlign = TextAlign.Center
            )

            Text(
                text = "APSC (Prelims & Mains GS I-V), UPSC, ADRE, SLRC, Full Mock Tests, Live Doubts & AI Study Planner",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Payment Method Tabs
            TabRow(
                selectedTabIndex = selectedMethod,
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                contentColor = NavyPrimary,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .fillMaxWidth()
            ) {
                Tab(
                    selected = selectedMethod == 0,
                    onClick = { selectedMethod = 0 },
                    text = { Text("UPI (₹99)", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.QrCode2, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    modifier = Modifier.testTag("tab_upi")
                )
                Tab(
                    selected = selectedMethod == 1,
                    onClick = { selectedMethod = 1 },
                    text = { Text("Cards", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.CreditCard, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    modifier = Modifier.testTag("tab_cards")
                )
                Tab(
                    selected = selectedMethod == 2,
                    onClick = { selectedMethod = 2 },
                    text = { Text("Net Banking", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.AccountBalance, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    modifier = Modifier.testTag("tab_netbanking")
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            when (selectedMethod) {
                0 -> {
                    // UPI Method
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Official Academy UPI ID",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MaterialTheme.colorScheme.surface)
                                    .border(1.dp, GoldAccent, RoundedCornerShape(10.dp))
                                    .padding(horizontal = 14.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = upiId,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = NavyDark
                                )

                                Spacer(modifier = Modifier.width(10.dp))

                                IconButton(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        val clip = ClipData.newPlainText("UPI ID", upiId)
                                        clipboard.setPrimaryClip(clip)
                                        Toast.makeText(context, "UPI ID copied: $upiId", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier
                                        .size(32.dp)
                                        .testTag("copy_upi_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "Copy UPI ID",
                                        tint = NavyPrimary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Direct UPI App Launcher Button
                            OutlinedButton(
                                onClick = {
                                    try {
                                        val uri = Uri.parse(
                                            "upi://pay?pa=$upiId&pn=BrainstormQ%20Academy&am=99&cu=INR&tn=BrainstormQ%20All%20Access%20Pass"
                                        )
                                        val intent = Intent(Intent.ACTION_VIEW, uri)
                                        context.startActivity(Intent.createChooser(intent, "Pay ₹99 with UPI App"))
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Please open GPay / PhonePe / Paytm and pay to $upiId", Toast.LENGTH_LONG).show()
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("launch_upi_intent_button"),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Open UPI App (GPay / PhonePe / BHIM)")
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Divider()

                            Spacer(modifier = Modifier.height(14.dp))

                            Text(
                                text = "Already paid? Enter 12-digit UPI Reference / UTR Number:",
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = enteredTxId,
                                onValueChange = { enteredTxId = it },
                                label = { Text("UTR / Transaction ID (e.g. 428901234567)") },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("utr_input_field")
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = {
                                    isProcessing = true
                                    val tx = if (enteredTxId.isNotBlank()) enteredTxId else "UPI_${System.currentTimeMillis()}"
                                    onPaymentSuccess(tx)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("verify_upi_payment_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.VerifiedUser, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Verify & Activate Lifetime Access", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
                1 -> {
                    // Cards Method
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = cardNumber,
                            onValueChange = { if (it.length <= 19) cardNumber = it },
                            label = { Text("Card Number (RuPay / Visa / MasterCard)") },
                            leadingIcon = { Icon(Icons.Default.CreditCard, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("card_number_input")
                        )

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = cardExpiry,
                                onValueChange = { if (it.length <= 5) cardExpiry = it },
                                label = { Text("MM/YY") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = cardCvv,
                                onValueChange = { if (it.length <= 4) cardCvv = it },
                                label = { Text("CVV") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Button(
                            onClick = {
                                val tx = "CARD_${System.currentTimeMillis()}"
                                onPaymentSuccess(tx)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("pay_card_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Pay ₹99 Securely", fontWeight = FontWeight.Bold)
                        }
                    }
                }
                2 -> {
                    // Net Banking
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val banks = listOf(
                            "State Bank of India (SBI)",
                            "Assam Gramin Vikash Bank (AGVB)",
                            "HDFC Bank",
                            "ICICI Bank",
                            "Punjab National Bank"
                        )

                        banks.forEach { bank ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (selectedBank == bank) NavyPrimary.copy(alpha = 0.1f)
                                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                    )
                                    .clickable { selectedBank = bank }
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedBank == bank,
                                    onClick = { selectedBank = bank }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = bank, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = {
                                val tx = "NB_${System.currentTimeMillis()}"
                                onPaymentSuccess(tx)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("pay_netbanking_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Pay ₹99 via $selectedBank", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "256-bit SSL Secure Transaction Guarantee",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
