<?php
session_start();
?>
<!DOCTYPE html>
<html>
<head>
    <title>Kommentare zu <?php echo $ontname; ?></title>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <!-- Bootstrap core CSS -->
    <link href="css/bootstrap.min.css" rel="stylesheet">

    <link href="css/main.css" rel="stylesheet">

    <!-- HTML5 shim and Respond.js IE8 support of HTML5 elements and media queries -->
    <!--[if lt IE 9]>
    <script src="https://oss.maxcdn.com/libs/html5shiv/3.7.0/html5shiv.js"></script>
    <script src="https://oss.maxcdn.com/libs/respond.js/1.4.2/respond.min.js"></script>
    <![endif]-->
</head>
<body>
<?php
$cmtx_identifier = $_GET['ontid'];
$cmtx_reference = $_GET['ontname'];
$cmtx_path = 'commentics/';
require $cmtx_path . 'includes/commentics.php'; //don't edit this line
?>
</body>

<!-- finally, more scripts -->
<!-- <script src="http://code.jquery.com/jquery-2.1.1.min.js"></script> -->
<script src="js/jquery-2.1.1.min.js"></script>
<script src="js/bootstrap.min.js"></script>
<script src="js/bootstrap-treeview.min.js"></script>

</html>
