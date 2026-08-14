package com.example

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Dataset
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoneyOff
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.AccessoryItem
import com.example.data.model.MobileItem
import com.example.data.model.RepairJob
import com.example.data.model.ShopProfile
import com.example.ui.components.BarcodeScannerModal
import com.example.ui.components.ConfirmationDialog
import com.example.ui.components.LanguageSelectionDialog
import com.example.ui.components.ThemeSelectionDialog
import com.example.ui.locale.AppLanguage
import com.example.ui.locale.LanguageStateManager
import com.example.ui.locale.LocalAppLanguage
import com.example.ui.locale.ProvideAppLanguageState
import com.example.ui.locale.stringRes
import com.example.ui.screens.accessories.AccessoriesScreen
import com.example.ui.screens.customers.CustomersScreen
import com.example.ui.screens.dashboard.DashboardScreen
import com.example.ui.screens.expenses.ExpensesScreen
import com.example.ui.screens.mobiles.MobileStockScreen
import com.example.ui.screens.rates.RateListScreen
import com.example.ui.screens.repairs.DailyRepairsScreen
import com.example.ui.screens.repairs.RepairsScreen
import com.example.ui.screens.reports.ReportsScreen
import com.example.ui.screens.sales.SalesPosScreen
import com.example.ui.theme.AppThemeMode
import com.example.ui.theme.BlackBackground
import com.example.ui.theme.DangerRed
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.GoldContainer
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.GoldSecondary
import com.example.ui.theme.KashifMobileTheme
import com.example.ui.theme.OnGoldContainer
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.ShopViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val viewModel: ShopViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val profile by viewModel.profileState.collectAsStateWithLifecycle()
            val currentLanguage = LanguageStateManager.currentLanguageState.value

            ProvideAppLanguageState(language = currentLanguage) {
                KashifMobileTheme {
                    MainAppContent(viewModel = viewModel, profile = profile)
                }
            }
        }
    }
}

sealed class Screen(val route: String, val titleKey: String, val icon: ImageVector) {
    object Dashboard : Screen("dashboard", "nav_dashboard", Icons.Default.Store)
    object Mobiles : Screen("mobiles", "nav_mobiles", Icons.Default.PhoneAndroid)
    object Accessories : Screen("accessories", "nav_accessories", Icons.Default.Headphones)
    object Repairs : Screen("repairs", "nav_repairs", Icons.Default.Build)
    object DailyRepairs : Screen("daily_repairs", "nav_daily_repairs", Icons.Default.Build)
    object Sales : Screen("sales", "nav_sales", Icons.Default.PointOfSale)
    object Customers : Screen("customers", "nav_customers", Icons.Default.People)
    object Rates : Screen("rates", "nav_rates", Icons.Default.ReceiptLong)
    object Expenses : Screen("expenses", "nav_expenses", Icons.Default.MoneyOff)
    object Reports : Screen("reports", "nav_reports", Icons.Default.Assessment)
}

@Composable
fun MainAppContent(viewModel: ShopViewModel, profile: ShopProfile) {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Dashboard) }
    var showGlobalScanner by remember { mutableStateOf(false) }
    var scannedResultItem by remember { mutableStateOf<Any?>(null) }
    var scannedBarcodeRaw by remember { mutableStateOf<String?>(null) }

    var showProfileDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showBackupDialog by remember { mutableStateOf(false) }
    var showLanguageDropdown by remember { mutableStateOf(false) }
    var showMoreMenu by remember { mutableStateOf(false) }
    var showDemoDataConfirm by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    val message by viewModel.message.collectAsStateWithLifecycle()
    val repairs by viewModel.repairsState.collectAsStateWithLifecycle()
    val cartItems by viewModel.cartItems.collectAsStateWithLifecycle()

    val pendingRepairsCount = remember(repairs) {
        repairs.count { it.status.equals("PENDING", ignoreCase = true) || it.status.equals("REPAIRING", ignoreCase = true) }
    }

    LaunchedEffect(message) {
        message?.let {
            snackbarHostState.showSnackbar(it, duration = SnackbarDuration.Short)
            viewModel.clearMessage()
        }
    }

    BackHandler(enabled = currentScreen !is Screen.Dashboard) {
        currentScreen = Screen.Dashboard
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = BlackBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopShopBar(
                profile = profile,
                currentScreen = currentScreen,
                onNavigate = { route ->
                    currentScreen = when (route) {
                        "mobiles" -> Screen.Mobiles
                        "accessories" -> Screen.Accessories
                        "repairs" -> Screen.Repairs
                        "daily_repairs" -> Screen.DailyRepairs
                        "sales" -> Screen.Sales
                        "customers" -> Screen.Customers
                        "rates" -> Screen.Rates
                        "expenses" -> Screen.Expenses
                        "reports" -> Screen.Reports
                        else -> Screen.Dashboard
                    }
                },
                onOpenScanner = { showGlobalScanner = true },
                onOpenProfile = { showProfileDialog = true },
                onOpenTheme = { showThemeDialog = true },
                onOpenLanguage = { showLanguageDialog = true },
                onToggleLanguage = { viewModel.toggleLanguage() },
                onOpenBackup = { showBackupDialog = true },
                onLanguageClick = { showLanguageDropdown = true },
                showLanguageDropdown = showLanguageDropdown,
                onDismissLanguageDropdown = { showLanguageDropdown = false },
                onSelectLanguage = { lang ->
                    viewModel.setLanguage(lang)
                    showLanguageDropdown = false
                },
                showMoreMenu = showMoreMenu,
                onToggleMoreMenu = { showMoreMenu = !showMoreMenu },
                onDismissMoreMenu = { showMoreMenu = false },
                onLoadDemoData = { showDemoDataConfirm = true }
            )
        },
        bottomBar = {
            ShopBottomNav(
                currentScreen = currentScreen,
                onScreenSelected = { screen -> currentScreen = screen },
                pendingRepairsCount = pendingRepairsCount,
                cartCount = cartItems.sumOf { it.quantity }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(BlackBackground)
        ) {
            Crossfade(targetState = currentScreen, label = "screen_fade") { screen ->
                when (screen) {
                    Screen.Dashboard -> DashboardScreen(
                        viewModel = viewModel,
                        onNavigate = { route ->
                            currentScreen = when (route) {
                                "mobiles" -> Screen.Mobiles
                                "accessories" -> Screen.Accessories
                                "repairs" -> Screen.Repairs
                                "daily_repairs" -> Screen.DailyRepairs
                                "sales" -> Screen.Sales
                                "customers" -> Screen.Customers
                                "rates" -> Screen.Rates
                                "expenses" -> Screen.Expenses
                                "reports" -> Screen.Reports
                                "backup" -> {
                                    showBackupDialog = true
                                    currentScreen
                                }
                                else -> Screen.Dashboard
                            }
                        },
                        onOpenScanner = { showGlobalScanner = true }
                    )
                    Screen.Mobiles -> MobileStockScreen(
                        viewModel = viewModel,
                        onNavigateToPos = { currentScreen = Screen.Sales }
                    )
                    Screen.Accessories -> AccessoriesScreen(
                        viewModel = viewModel,
                        onNavigateToPos = { currentScreen = Screen.Sales }
                    )
                    Screen.Repairs -> RepairsScreen(
                        viewModel = viewModel,
                        onNavigateToDaily = { currentScreen = Screen.DailyRepairs }
                    )
                    Screen.DailyRepairs -> DailyRepairsScreen(
                        viewModel = viewModel,
                        onBack = { currentScreen = Screen.Repairs }
                    )
                    Screen.Sales -> SalesPosScreen(viewModel = viewModel)
                    Screen.Customers -> CustomersScreen(viewModel = viewModel)
                    Screen.Rates -> RateListScreen(viewModel = viewModel)
                    Screen.Expenses -> ExpensesScreen(viewModel = viewModel)
                    Screen.Reports -> ReportsScreen(viewModel = viewModel)
                }
            }
        }
    }

    // Global Barcode Scanner Modal
    if (showGlobalScanner) {
        BarcodeScannerModal(
            onDismiss = { showGlobalScanner = false },
            onBarcodeScanned = { barcode ->
                showGlobalScanner = false
                scannedBarcodeRaw = barcode
                coroutineScope.launch {
                    val result = viewModel.findProductByBarcode(barcode)
                    scannedResultItem = result
                }
            }
        )
    }

    // Global Scanned Item Result Dialog
    if (scannedBarcodeRaw != null) {
        ScannedBarcodeResultDialog(
            barcode = scannedBarcodeRaw!!,
            foundItem = scannedResultItem,
            currency = profile.currency,
            onDismiss = {
                scannedBarcodeRaw = null
                scannedResultItem = null
            },
            onAddToCart = { item ->
                when (item) {
                    is MobileItem -> {
                        viewModel.addMobileToCart(item)
                        currentScreen = Screen.Sales
                    }
                    is AccessoryItem -> {
                        viewModel.addAccessoryToCart(item)
                        currentScreen = Screen.Sales
                    }
                }
                scannedBarcodeRaw = null
                scannedResultItem = null
            },
            onNavigateToItem = { item ->
                when (item) {
                    is MobileItem -> currentScreen = Screen.Mobiles
                    is AccessoryItem -> currentScreen = Screen.Accessories
                    is RepairJob -> currentScreen = Screen.Repairs
                }
                scannedBarcodeRaw = null
                scannedResultItem = null
            }
        )
    }

    // Shop Profile Dialog
    if (showProfileDialog) {
        ShopProfileEditDialog(
            profile = profile,
            onDismiss = { showProfileDialog = false },
            onSave = { updated ->
                viewModel.updateProfile(updated)
                showProfileDialog = false
            },
            onOpenTheme = {
                showThemeDialog = true
            },
            onOpenLanguage = {
                showLanguageDialog = true
            }
        )
    }

    // Language Selection Dialog
    if (showLanguageDialog) {
        LanguageSelectionDialog(
            currentLanguage = LanguageStateManager.currentLanguageState.value,
            onSelectLanguage = { lang ->
                viewModel.setLanguage(lang)
                showLanguageDialog = false
            },
            onDismiss = { showLanguageDialog = false }
        )
    }

    // Theme Selection Dialog
    if (showThemeDialog) {
        ThemeSelectionDialog(
            currentThemeId = profile.theme,
            onSelectTheme = { themeMode ->
                viewModel.setTheme(themeMode)
            },
            onDismiss = { showThemeDialog = false }
        )
    }

    // Backup & Restore Dialog
    if (showBackupDialog) {
        BackupRestoreDialog(
            viewModel = viewModel,
            onDismiss = { showBackupDialog = false }
        )
    }

    // Demo Data Confirmation Dialog
    if (showDemoDataConfirm) {
        ConfirmationDialog(
            title = stringRes("load_demo_data"),
            message = "This will load realistic sample demo inventory (iPhones, Samsung, Redmi mobiles, chargers, accessories, repair jobs, and standard rates) into the app.",
            confirmText = stringRes("confirm"),
            onConfirm = {
                viewModel.loadSampleDemoData()
                showDemoDataConfirm = false
            },
            onDismiss = { showDemoDataConfirm = false }
        )
    }
}

@Composable
fun TopShopBar(
    profile: ShopProfile,
    currentScreen: Screen,
    onNavigate: (String) -> Unit,
    onOpenScanner: () -> Unit,
    onOpenProfile: () -> Unit,
    onOpenTheme: () -> Unit,
    onOpenLanguage: () -> Unit,
    onToggleLanguage: () -> Unit,
    onOpenBackup: () -> Unit,
    onLanguageClick: () -> Unit,
    showLanguageDropdown: Boolean,
    onDismissLanguageDropdown: () -> Unit,
    onSelectLanguage: (AppLanguage) -> Unit,
    showMoreMenu: Boolean,
    onToggleMoreMenu: () -> Unit,
    onDismissMoreMenu: () -> Unit,
    onLoadDemoData: () -> Unit
) {
    val currentLang = LocalAppLanguage.current

    Surface(
        color = DarkSurfaceVariant,
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Brand Logo & Title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clickable { onNavigate("dashboard") }
                    .padding(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(GoldPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_shop_logo),
                        contentDescription = "Logo",
                        modifier = Modifier.size(36.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = profile.shopName,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        color = GoldPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${profile.ownerName} • ${profile.city}",
                        fontSize = 11.sp,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Action Icons: Scanner, Theme, Language Switcher Pill, More Menu
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(
                    onClick = onOpenScanner,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(GoldContainer)
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCodeScanner,
                        contentDescription = "Barcode Scanner",
                        tint = OnGoldContainer,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Theme Selector Button
                IconButton(
                    onClick = onOpenTheme,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = "Change Theme",
                        tint = GoldPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Quick Language Toggle Pill
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = GoldContainer.copy(alpha = 0.35f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary),
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onToggleLanguage() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = "Language",
                            tint = GoldPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = currentLang.flagEmoji,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = GoldPrimary
                        )
                    }
                }

                // More Menu (Rates, Customers, Expenses, Reports, Profile, Theme, Backup, Demo Data)
                Box {
                    IconButton(
                        onClick = onToggleMoreMenu,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Menu",
                            tint = TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showMoreMenu,
                        onDismissRequest = onDismissMoreMenu,
                        modifier = Modifier.background(DarkSurfaceVariant)
                    ) {
                        DropdownMenuItem(
                            leadingIcon = { Icon(Icons.Default.ReceiptLong, null, tint = GoldPrimary, modifier = Modifier.size(18.dp)) },
                            text = { Text(stringRes("nav_rates"), fontSize = 13.sp, color = TextPrimary) },
                            onClick = {
                                onDismissMoreMenu()
                                onNavigate("rates")
                            }
                        )
                        DropdownMenuItem(
                            leadingIcon = { Icon(Icons.Default.People, null, tint = Color(0xFF60A5FA), modifier = Modifier.size(18.dp)) },
                            text = { Text(stringRes("nav_customers"), fontSize = 13.sp, color = TextPrimary) },
                            onClick = {
                                onDismissMoreMenu()
                                onNavigate("customers")
                            }
                        )
                        DropdownMenuItem(
                            leadingIcon = { Icon(Icons.Default.MoneyOff, null, tint = DangerRed, modifier = Modifier.size(18.dp)) },
                            text = { Text(stringRes("nav_expenses"), fontSize = 13.sp, color = TextPrimary) },
                            onClick = {
                                onDismissMoreMenu()
                                onNavigate("expenses")
                            }
                        )
                        DropdownMenuItem(
                            leadingIcon = { Icon(Icons.Default.Assessment, null, tint = SuccessGreen, modifier = Modifier.size(18.dp)) },
                            text = { Text(stringRes("nav_reports"), fontSize = 13.sp, color = TextPrimary) },
                            onClick = {
                                onDismissMoreMenu()
                                onNavigate("reports")
                            }
                        )
                        DropdownMenuItem(
                            leadingIcon = { Icon(Icons.Default.Build, null, tint = Color(0xFFFB923C), modifier = Modifier.size(18.dp)) },
                            text = { Text(stringRes("nav_daily_repairs"), fontSize = 13.sp, color = TextPrimary) },
                            onClick = {
                                onDismissMoreMenu()
                                onNavigate("daily_repairs")
                            }
                        )
                        HorizontalDivider(color = DarkBorder)
                        DropdownMenuItem(
                            leadingIcon = { Icon(Icons.Default.Language, null, tint = GoldPrimary, modifier = Modifier.size(18.dp)) },
                            text = { Text("Language / زبان / اللغة", fontSize = 13.sp, color = TextPrimary) },
                            onClick = {
                                onDismissMoreMenu()
                                onOpenLanguage()
                            }
                        )
                        DropdownMenuItem(
                            leadingIcon = { Icon(Icons.Default.Palette, null, tint = GoldPrimary, modifier = Modifier.size(18.dp)) },
                            text = { Text("Theme & Style", fontSize = 13.sp, color = TextPrimary) },
                            onClick = {
                                onDismissMoreMenu()
                                onOpenTheme()
                            }
                        )
                        DropdownMenuItem(
                            leadingIcon = { Icon(Icons.Default.Settings, null, tint = GoldSecondary, modifier = Modifier.size(18.dp)) },
                            text = { Text(stringRes("nav_profile"), fontSize = 13.sp, color = TextPrimary) },
                            onClick = {
                                onDismissMoreMenu()
                                onOpenProfile()
                            }
                        )
                        DropdownMenuItem(
                            leadingIcon = { Icon(Icons.Default.Backup, null, tint = Color(0xFFA78BFA), modifier = Modifier.size(18.dp)) },
                            text = { Text(stringRes("nav_backup"), fontSize = 13.sp, color = TextPrimary) },
                            onClick = {
                                onDismissMoreMenu()
                                onOpenBackup()
                            }
                        )
                        HorizontalDivider(color = DarkBorder)
                        DropdownMenuItem(
                            leadingIcon = { Icon(Icons.Default.Dataset, null, tint = GoldPrimary, modifier = Modifier.size(18.dp)) },
                            text = { Text(stringRes("load_demo_data"), fontSize = 13.sp, color = TextPrimary) },
                            onClick = {
                                onDismissMoreMenu()
                                onLoadDemoData()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ShopBottomNav(
    currentScreen: Screen,
    onScreenSelected: (Screen) -> Unit,
    pendingRepairsCount: Int,
    cartCount: Int
) {
    val navItems = listOf(
        Screen.Dashboard,
        Screen.Mobiles,
        Screen.Accessories,
        Screen.Repairs,
        Screen.Sales
    )

    NavigationBar(
        containerColor = DarkSurfaceVariant,
        tonalElevation = 8.dp
    ) {
        navItems.forEach { screen ->
            val isSelected = currentScreen == screen
            NavigationBarItem(
                selected = isSelected,
                onClick = { onScreenSelected(screen) },
                icon = {
                    if (screen == Screen.Repairs && pendingRepairsCount > 0) {
                        BadgedBox(
                            badge = {
                                Badge(
                                    containerColor = Color(0xFFFB923C),
                                    contentColor = Color.Black
                                ) {
                                    Text("$pendingRepairsCount", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        ) {
                            Icon(screen.icon, contentDescription = stringRes(screen.titleKey), modifier = Modifier.size(22.dp))
                        }
                    } else if (screen == Screen.Sales && cartCount > 0) {
                        BadgedBox(
                            badge = {
                                Badge(
                                    containerColor = GoldPrimary,
                                    contentColor = Color.Black
                                ) {
                                    Text("$cartCount", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        ) {
                            Icon(screen.icon, contentDescription = stringRes(screen.titleKey), modifier = Modifier.size(22.dp))
                        }
                    } else {
                        Icon(screen.icon, contentDescription = stringRes(screen.titleKey), modifier = Modifier.size(22.dp))
                    }
                },
                label = {
                    Text(
                        text = stringRes(screen.titleKey),
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = DarkSurface,
                    selectedTextColor = GoldPrimary,
                    indicatorColor = GoldPrimary,
                    unselectedIconColor = TextSecondary,
                    unselectedTextColor = TextMuted
                )
            )
        }
    }
}

@Composable
fun ScannedBarcodeResultDialog(
    barcode: String,
    foundItem: Any?,
    currency: String,
    onDismiss: () -> Unit,
    onAddToCart: (Any) -> Unit,
    onNavigateToItem: (Any) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Scanned Barcode",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldPrimary
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, null, tint = TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DarkSurfaceVariant, RoundedCornerShape(8.dp))
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = barcode,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (foundItem != null) {
                    when (foundItem) {
                        is MobileItem -> {
                            Text(
                                text = "Product: ${foundItem.brand} ${foundItem.model}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Color: ${foundItem.color} | Storage: ${foundItem.storage}",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                            Text(
                                text = "Price: $currency ${String.format("%.2f", foundItem.salePrice)} • Stock: ${foundItem.quantity}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = SuccessGreen
                            )

                            Spacer(modifier = Modifier.height(16.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { onNavigateToItem(foundItem) },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary)
                                ) {
                                    Text("View Stock")
                                }
                                Button(
                                    onClick = { onAddToCart(foundItem) },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = Color.Black)
                                ) {
                                    Text("Add to POS", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        is AccessoryItem -> {
                            Text(
                                text = "Product: ${foundItem.name}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Category: ${foundItem.category}",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                            Text(
                                text = "Price: $currency ${String.format("%.2f", foundItem.salePrice)} • Stock: ${foundItem.quantity}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = SuccessGreen
                            )

                            Spacer(modifier = Modifier.height(16.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { onNavigateToItem(foundItem) },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary)
                                ) {
                                    Text("View Stock")
                                }
                                Button(
                                    onClick = { onAddToCart(foundItem) },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = Color.Black)
                                ) {
                                    Text("Add to POS", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        is RepairJob -> {
                            Text(
                                text = "Repair Token: ${foundItem.jobCode}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Customer: ${foundItem.customerName} (${foundItem.customerPhone})",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                            Text(
                                text = "Device: ${foundItem.mobileBrand} ${foundItem.mobileModel} - ${foundItem.customerProblem}",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                            Text(
                                text = "Cost: $currency ${String.format("%.2f", foundItem.repairCost)} • Remaining: $currency ${String.format("%.2f", foundItem.remainingPayment)}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFB923C)
                            )

                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { onNavigateToItem(foundItem) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = Color.Black)
                            ) {
                                Text("Open Repair Job Details", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } else {
                    Text(
                        text = "Barcode not found in current inventory or repair tokens.",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = DarkBorder, contentColor = TextPrimary)
                    ) {
                        Text("Close")
                    }
                }
            }
        }
    }
}

@Composable
fun ShopProfileEditDialog(
    profile: ShopProfile,
    onDismiss: () -> Unit,
    onSave: (ShopProfile) -> Unit,
    onOpenTheme: (() -> Unit)? = null,
    onOpenLanguage: (() -> Unit)? = null
) {
    var shopName by remember { mutableStateOf(profile.shopName) }
    var ownerName by remember { mutableStateOf(profile.ownerName) }
    var phoneNumber by remember { mutableStateOf(profile.phoneNumber) }
    var whatsappNumber by remember { mutableStateOf(profile.whatsappNumber) }
    var emailAddress by remember { mutableStateOf(profile.emailAddress) }
    var shopAddress by remember { mutableStateOf(profile.shopAddress) }
    var city by remember { mutableStateOf(profile.city) }
    var currency by remember { mutableStateOf(profile.currency) }
    var invoiceTerms by remember { mutableStateOf(profile.invoiceTerms) }

    val currentTheme = remember(profile.theme) { AppThemeMode.fromId(profile.theme) }
    val currentLang = LanguageStateManager.currentLanguageState.value

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 600.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringRes("nav_profile"),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldPrimary
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, null, tint = TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = shopName,
                    onValueChange = { shopName = it },
                    label = { Text(stringRes("shop_name")) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldPrimary,
                        unfocusedBorderColor = DarkBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = ownerName,
                    onValueChange = { ownerName = it },
                    label = { Text(stringRes("owner_name")) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldPrimary,
                        unfocusedBorderColor = DarkBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = { phoneNumber = it },
                        label = { Text("Phone") },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldPrimary,
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                    OutlinedTextField(
                        value = whatsappNumber,
                        onValueChange = { whatsappNumber = it },
                        label = { Text(stringRes("whatsapp")) },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldPrimary,
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = emailAddress,
                    onValueChange = { emailAddress = it },
                    label = { Text(stringRes("email")) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldPrimary,
                        unfocusedBorderColor = DarkBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = city,
                        onValueChange = { city = it },
                        label = { Text(stringRes("city")) },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldPrimary,
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                    OutlinedTextField(
                        value = currency,
                        onValueChange = { currency = it.uppercase() },
                        label = { Text(stringRes("currency")) },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldPrimary,
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = shopAddress,
                    onValueChange = { shopAddress = it },
                    label = { Text(stringRes("customer_address")) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldPrimary,
                        unfocusedBorderColor = DarkBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = invoiceTerms,
                    onValueChange = { invoiceTerms = it },
                    label = { Text(stringRes("terms_footer")) },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldPrimary,
                        unfocusedBorderColor = DarkBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Theme selection card
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = DarkSurfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = onOpenTheme != null) { onOpenTheme?.invoke() }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Palette,
                                contentDescription = null,
                                tint = GoldPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Active Theme",
                                    fontSize = 12.sp,
                                    color = TextMuted
                                )
                                Text(
                                    text = currentTheme.title,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            }
                        }

                        if (onOpenTheme != null) {
                            Text(
                                text = "Change",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = GoldPrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Language selection card
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = DarkSurfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = onOpenLanguage != null) { onOpenLanguage?.invoke() }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Language,
                                contentDescription = null,
                                tint = GoldPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = stringRes("select_language"),
                                    fontSize = 12.sp,
                                    color = TextMuted
                                )
                                Text(
                                    text = "${currentLang.flagEmoji} ${currentLang.displayName} (${currentLang.nativeName})",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            }
                        }

                        if (onOpenLanguage != null) {
                            Text(
                                text = "Change",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = GoldPrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary)
                    ) {
                        Text(stringRes("cancel"))
                    }
                    Button(
                        onClick = {
                            onSave(
                                profile.copy(
                                    shopName = shopName.ifBlank { "KASHIF MOBILE AND REPAIR" },
                                    ownerName = ownerName.ifBlank { "Muhammad Kashif" },
                                    phoneNumber = phoneNumber,
                                    whatsappNumber = whatsappNumber,
                                    emailAddress = emailAddress,
                                    shopAddress = shopAddress,
                                    city = city,
                                    currency = currency.ifBlank { "SAR" },
                                    invoiceTerms = invoiceTerms
                                )
                            )
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = Color.Black)
                    ) {
                        Text(stringRes("save"), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun BackupRestoreDialog(
    viewModel: ShopViewModel,
    onDismiss: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    var backupJson by remember { mutableStateOf("") }
    var restoreJsonInput by remember { mutableStateOf("") }
    var isExporting by remember { mutableStateOf(false) }
    var isRestoring by remember { mutableStateOf(false) }
    var showConfirmRestore by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 600.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringRes("nav_backup"),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldPrimary
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, null, tint = TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Backup your entire shop database (mobiles stock, accessories, repair jobs, invoices, customer records, standard rates, and expenses) to offline JSON format.",
                    fontSize = 12.sp,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(16.dp))

                // EXPORT SECTION
                Button(
                    onClick = {
                        coroutineScope.launch {
                            isExporting = true
                            backupJson = viewModel.exportBackupJson()
                            isExporting = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = Color.Black)
                ) {
                    Icon(Icons.Default.Backup, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isExporting) "Generating Backup..." else stringRes("export_backup"), fontWeight = FontWeight.Bold)
                }

                if (backupJson.isNotBlank()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = backupJson,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldPrimary,
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextSecondary
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Button(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("KashifShopBackup", backupJson))
                            Toast.makeText(context, "Backup JSON copied to clipboard!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = DarkBorder, contentColor = GoldPrimary)
                    ) {
                        Icon(Icons.Default.ContentCopy, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Copy Backup to Clipboard")
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
                HorizontalDivider(color = DarkBorder)
                Spacer(modifier = Modifier.height(14.dp))

                // RESTORE SECTION
                Text(
                    text = stringRes("restore_backup"),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = restoreJsonInput,
                    onValueChange = { restoreJsonInput = it },
                    placeholder = { Text("Paste your backup JSON here...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldPrimary,
                        unfocusedBorderColor = DarkBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = {
                        if (restoreJsonInput.isNotBlank()) {
                            showConfirmRestore = true
                        } else {
                            Toast.makeText(context, "Please paste valid backup JSON first", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = DangerRed, contentColor = Color.White)
                ) {
                    Icon(Icons.Default.Restore, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringRes("restore_backup"), fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (showConfirmRestore) {
        ConfirmationDialog(
            title = stringRes("warning"),
            message = stringRes("restore_confirm"),
            confirmText = stringRes("confirm"),
            onConfirm = {
                showConfirmRestore = false
                coroutineScope.launch {
                    val ok = viewModel.restoreBackupJson(restoreJsonInput)
                    if (ok) {
                        Toast.makeText(context, "Data restored successfully!", Toast.LENGTH_LONG).show()
                        onDismiss()
                    } else {
                        Toast.makeText(context, "Invalid JSON data format", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            onDismiss = { showConfirmRestore = false }
        )
    }
}
