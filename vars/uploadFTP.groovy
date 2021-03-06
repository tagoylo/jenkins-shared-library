def call(project, filename) {
    root = ''
    ext = ''

    if (project == 'hdl') {
        target = root + 'hdl'
    }
  else if (project == 'TransceiverToolbox') {
        ext = '.mltbx'
        target = 'toolboxes/trx/'

        // Determine branch
        def branch = env.BRANCH_NAME
        if (!env.BRANCH_NAME) {
            println('Branch name not found in environment, checking through git')
            sh 'git branch > branchname'
            sh 'sed -i "s/[*]//" branchname'
            branch = readFile('branchname').trim()
        }
        println('Found branch: ' + branch)
        if (branch == 'master') {
            target = target + 'master'
        }
    else {
            target = target + 'dev'
    }
  }
  else if (project == 'HighSpeedConverterToolbox') {
        ext = '.mltbx'
        target = 'toolboxes/hsx/'

        // Determine branch
        def branch = env.BRANCH_NAME
        if (!env.BRANCH_NAME) {
            println('Branch name not found in environment, checking through git')
            sh 'git branch > branchname'
            sh 'sed -i "s/[*]//" branchname'
            branch = readFile('branchname').trim()
        }
        println('Found branch: ' + branch)
        if (branch == 'master') {
            target = target + 'master'
        }
    else {
            target = target + 'dev'
    }
  }
  else {
        println('Unknown project... returning')
        return
  }
    println('---FTP pre-target root')
    println(target)

    // Check if we have files to upload based on target
    sh 'ls ' + filename + ' > files_searched || true'
    def files_list = readFile('files_searched')
    println('Files found: ' + files_list)
    if (files_list.length() <= 0) {
        println('No files to upload... returning')
        return
    }

    // Set FTP settings
    withCredentials([usernamePassword(credentialsId: 'FTP_USER', passwordVariable: 'FTP_PASS', usernameVariable: 'FTP_USERNAME')]) {
        withCredentials([string(credentialsId: 'FTP_SERVER', variable: 'FTP_SERVER')]) {
            withCredentials([string(credentialsId: 'FTP_ROOT_TARGET', variable: 'FTP_ROOT_TARGET')]) {
                upload_target = FTP_ROOT_TARGET + target
                println("Uploading: $filename")
                println("Target: $upload_target")

                commands = 'set ssl:verify-certificate no; set ftp:ssl-allow no; cd ' + upload_target + '; mput ' + filename + '; bye'
                sh 'lftp -e "'+commands+'" -u $FTP_USERNAME,$FTP_PASS $FTP_SERVER'
  }}}
        }
